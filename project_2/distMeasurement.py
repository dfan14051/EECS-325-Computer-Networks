import socket
import struct
import select
import time
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import csv

def save_data(hopcounts, rtts):
    with open('data.csv', 'w') as csvfile:
        writer = csv.writer(csvfile, delimiter=' ')
        for i in range(len(hopcounts)):
            writer.writerow([str(hopcounts[i]), str(rtts[i])])

def read_hosts_to_ips():
    f = open('targets.txt')
    ips = []
    hostnames = []
    for line in f:
        hostnames.append(line.rstrip())
        ips.append(socket.gethostbyname(line.rstrip()))
    f.close()
    return hostnames,ips

DATAGRAM_TTL = 128 
PORT = 33434 
MESSAGE_COURTESY = "measurement for class project. questions to student dmf98@case.edu or professor mxr136@case.edu"
RETRIES = 4
LOCAL_IP = '10.4.3.102'
def main():
    hostnames, ips = read_hosts_to_ips()
    rtts = []
    hopcounts = []
    message = bytes(MESSAGE_COURTESY + 'a'*(1472 - len(MESSAGE_COURTESY)),'ascii')
    datagram = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.getprotobyname('udp'))
    datagram.setsockopt(socket.SOL_IP, socket.IP_TTL, DATAGRAM_TTL)
    recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_ICMP)
    recv_socket.settimeout(1)
    recv_socket.bind(("",0))
    for i in range(len(ips)):
        ip = ips[i]
        hostname = hostnames[i]
        print("Testing %s" % hostname)
        tries = 0
        finished = False
        while tries < RETRIES and not finished:
            try:
                sending_time = time.time()
                datagram.sendto(message, (ip, PORT))
                packet = recv_socket.recv(1500)
                receive_time = time.time()
                rtt = 1000 * (receive_time - sending_time)
                rtts.append(rtt)
                icmp_type = packet[20]
                icmp_code = packet[21]
                returned_ttl = packet[36]
                hop_count = DATAGRAM_TTL - returned_ttl
                hopcounts.append(hop_count)
                source_ipaddr = socket.inet_ntoa(packet[12:16])
                dest_ipaddr = socket.inet_ntoa(packet[16:20])
                resp_source_ipaddr = socket.inet_ntoa(packet[40:44])
                resp_dest_ipaddr = socket.inet_ntoa(packet[44:48])
                dest_port = struct.unpack("!H", packet[50:52])[0]

                if(icmp_type != 3 or icmp_code != 3):
                    print("Error in ICMP packet")
                    break

                if(source_ipaddr != ip or dest_ipaddr != LOCAL_IP or resp_source_ipaddr != LOCAL_IP or resp_dest_ipaddr != ip):
                    print("Unexpected Packet Received")
                    raise socket.error
                print("%i Bytes Received" % len(packet[28:]))
                print("RTT(ms): %f" % rtt)
                print("Hop Count: %i" % hop_count)
                finished = True
            except socket.error:
                tries += 1
        if not finished:
            print("Timed out")
        print(30*"-")
    datagram.close()
    recv_socket.close()

    save_data(hopcounts, rtts)
    hopcounts, rtts = (list(x) for x in zip(*sorted(zip(hopcounts, rtts), key=lambda pair: pair[0])))
    hopcounts_fit = np.asarray(hopcounts, dtype=float)
    rtts = np.asarray(rtts)

    fig = plt.figure()
    subplot = plt.subplot(111)
    subplot.set_title('RTT vs Hop Count')
    subplot.set_xlabel('Hop Count')
    subplot.set_ylabel('RTT(ms)')
    fit = np.polyfit(hopcounts_fit, rtts, deg=1)
    subplot.plot(hopcounts_fit, fit[0] * hopcounts_fit + fit[1], color='red')
    subplot.scatter(hopcounts, rtts)
    fig.savefig('figures/rtt_vs_hops.png')

if __name__=="__main__":
    main()
