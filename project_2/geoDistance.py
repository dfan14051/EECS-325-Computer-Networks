import socket
import geoip2.database
import urllib.request 
import csv
import math
import matplotlib
matplotlib.use('Agg')
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import linregress

def read_hosts_to_ips():
    f = open('targets.txt')
    ips = []
    hostnames = []
    for line in f:
        hostnames.append(line.rstrip())
        ips.append(socket.gethostbyname(line.rstrip()))
    f.close()
    return hostnames,ips

def get_my_ip():
    response = urllib.request.urlopen("http://checkip.amazonaws.com/").read().decode("utf-8").rstrip()
    print("IP Address of Current Machine: %s\n" % response)
    return response

def read_data_file():
    hopcounts = []
    rtts= []
    with open('data.csv') as csvfile:
        reader = csv.reader(csvfile, delimiter= ' ')
        for row in reader:
            hopcounts.append(int(row[0]))
            rtts.append(float(row[1]))
    return (hopcounts, rtts)
        
def haversine_distance(lat1, lon1, lat2, lon2):
    delta_lat = lat2 - lat1
    delta_lon = lon2 - lon1
    a = math.sin(delta_lat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(delta_lon/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    # mean radius of the earth in km
    R = 6371
    distance = R * c
    return distance

CASE_IP = '129.22.12.21'
def main():
    hostnames, ips = read_hosts_to_ips()
    my_ip = get_my_ip()
    reader = geoip2.database.Reader('../GeoLite2-City.mmdb')

    # Check that Case's IP maps to Cleveland
    case_city = reader.city(CASE_IP).city.name
    print("Sanity Check...")
    print("%s maps to %s\n" % (CASE_IP, case_city))

    # Get my coords
    my_loc = reader.city(my_ip)
    my_lat = my_loc.location.latitude
    my_lon = my_loc.location.longitude
    print("Coordinates of current machine: %f, %f" % (my_lat, my_lon))

    # Get target coords
    coordinates = []
    for i in range(len(ips)):
        ip = ips[i]
        hostname = hostnames[i]
        ip_loc = reader.city(ip)
        ip_lat = ip_loc.location.latitude
        ip_lon = ip_loc.location.longitude
        print("Coordinates of %s: %f, %f" % (hostname, ip_lat, ip_lon))
        coordinates.append((ip_lat, ip_lon))
    
    # Get CWRU coords
    cwru_loc = reader.city(CASE_IP)
    cwru_lat = cwru_loc.location.latitude
    cwru_lon = cwru_loc.location.longitude
    print("Coordinates of CWRU: %f, %f\n" % (cwru_lat, cwru_lon))

    # Get distance from machine to CWRU
    machine_to_cwru_dist = haversine_distance(my_lat, my_lon, cwru_lat, cwru_lon)

    direct_distances = []
    indirect_distances = []
    count = 0
    # Calculate distances using the haversine formula
    for (lat, lon) in coordinates:
        direct_dist = haversine_distance(my_lat, my_lon, lat, lon) 
        print("Direct distance to %s: %f km" % (hostnames[count], direct_dist))
        direct_distances.append(direct_dist)

        indirect_dist = machine_to_cwru_dist + haversine_distance(cwru_lat, cwru_lon, lat, lon)
        print("Indirect distance to %s: %f km" % (hostnames[count], indirect_dist))
        indirect_distances.append(indirect_dist)
        print("-"*50)
        count += 1 

    # Get data from other script
    hopcounts, rtts = read_data_file()
    
    # Remove sites that timed out for the other script
    direct_distances.pop(hostnames.index('repubblica.it'))
    indirect_distances.pop(hostnames.index('repubblica.it'))
    direct_distances.pop(hostnames.index('gome.com.cn'))
    indirect_distances.pop(hostnames.index('gome.com.cn'))

    # Scatter plots
    fig = plt.figure()
    subplot = plt.subplot(111)
    subplot.set_title('Hop Count vs Direct Geodistance')
    subplot.set_xlabel('Direct Geodistance (km)')
    subplot.set_ylabel('Hop Count')
    subplot.scatter(direct_distances, hopcounts)
    fig.savefig('figures/direct_vs_hops.png')

    fig = plt.figure()
    subplot = plt.subplot(111)
    subplot.set_title('Hop Count vs Indirect Geodistance')
    subplot.set_xlabel('Indirect Geodistance (km)')
    subplot.set_ylabel('Hop Count')
    subplot.scatter(indirect_distances, hopcounts)
    fig.savefig('figures/indirect_vs_hops.png')

    # Matplotlib is being really dumb so the following lines are to just try to not have the y axis be a million sigfigs long
    hopcounts_sorted, rtts = (list(x) for x in zip(*sorted(zip(hopcounts, rtts), key=lambda pair: pair[0])))
    _, direct_distances = (list(x) for x in zip(*sorted(zip(hopcounts, direct_distances), key=lambda pair: pair[0])))
    _, indirect_distances = (list(x) for x in zip(*sorted(zip(hopcounts, indirect_distances), key=lambda pair: pair[0])))

    for x in range(len(rtts)):
        rtts[x] = float(str(rtts[x])[:5])

    fig = plt.figure()
    subplot = plt.subplot(111)
    subplot.set_title('RTT(ms) vs Indirect Geodistance')
    subplot.set_xlabel('Indirect Geodistance (km)')
    subplot.set_ylabel('RTT (ms)')
    subplot.scatter(indirect_distances, rtts)
    fig.savefig('figures/indirect_vs_rtt.png')

    fig = plt.figure()
    subplot = plt.subplot(111)
    subplot.set_title('RTT(ms) vs Direct Geodistance')
    subplot.set_xlabel('Direct Geodistance (km)')
    subplot.set_ylabel('RTT(ms)')
    subplot.scatter(direct_distances, rtts)
    fig.savefig('figures/direct_vs_rtt.png')

    # Correlation coefficients
    r_hops_rtt = linregress(hopcounts_sorted, rtts)[2]
    r_direct_hops = linregress(direct_distances, hopcounts_sorted)[2]
    r_indirect_hops = linregress(indirect_distances, hopcounts_sorted)[2]
    r_direct_RTT = linregress(direct_distances, rtts)[2]
    r_indirect_RTT = linregress(indirect_distances, rtts)[2]

    print("r(direct_dist, hops): %f" % r_direct_hops)
    print("r(indirect_dist, hops): %f" % r_indirect_hops)
    print("r(direct_dist, RTT): %f" % r_direct_RTT)
    print("r(indirect_dist, RTT): %f" % r_indirect_RTT)
    print("r(hops, RTT): %f" % r_hops_rtt)

if __name__=='__main__':
    main()
