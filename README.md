# GetNet

This module provide a tools to download a BDTopo network extract from WFS.

<h2>User's guide</h2>

<h3>With the graphical front-end</h3>

- Download and copy the setup file on your computer 
- double-click on the file
- Enter the IGN API key valid. With this key you have to have access to the WFS with the resource <i><b>BDTOPO_BDD_WLD_WGS84G:route</b></i>
- if your are behind a proxy, enter the address
- click on "ok"
- by right clicking, select a bbox
- 

<h3>With the command-line interface</h3>

```shell

java -Dhttp.proxyHost=proxy.ign.fr -Dhttp.proxyPort=3128 -jar getnet.jar PRATIQUE 2.39837991 48.74964230 2.44213927 48.78402541 0.01 "network.wkt" 4326

```