# Lil'Greedy
Tubes 1 Strategi algoritma, pengimplementasian algoritma greedy untuk membuat sebuah bot permainan galaxio dengan menggunakan bahasa java.


  
- [Lil'Greedy](#lilgreedy)
  - [Authors](#authors)
  - [GGA (Galaxio's Greedy Algorithm) yang Dipakai](#gga-galaxios-greedy-algorithm-yang-dipakai)
  - [Requirements](#requirements)
  - [Compile](#compile)

## Authors

- Hidayatullah Wildan Ghaly Buchary - 13521015
- Raditya Naufal Abiyu - 13521022
- Fatih Nararya Rashadyfa I. - 13521060

## GGA (Galaxio's Greedy Algorithm) yang Dipakai

Prioritas dari _bot_ secara berurutan adalah sebagai berikut :

1. Jika bot berada dekat dengan perbatasan, maka bergerak ke titik tengah peta
2. Jika ukuran bot cukup besar dan belum ada teleporter, maka tembakkan teleporter ke arah musuh.
3. Jika sudah ada teleporter dan teleporter berada di dekat player yang besar, maka jangan lakukan teleportasi. Tetapi, jika teleporter berada di dekat player yang kecil, maka lakukan teleportasi.
4. Jika ada torpedo yang dekat dan mengarah ke diri kita, maka aktifkan pelindung jika ukuran bot terlalu besar untuk bisa kabur dengan mudah. Tetapi, jika ukuran bot masih cukup kecil sehingga masih cepat maka bot akan menghindar dengan berbelok sejauh 90 derajat.
5. Jika bot dekat dengan musuh dan bot masih punya size untuk menembak maka bot akan menembak. Jika bot tidak punya size untuk menembak maka bot akan melarikan diri.
6. Akan ada validasi kedua untuk border, jika bot sudah berada di daerah borde maka bot akan mengarah ke tengah.
7. Jika bot berada di sekitar gass cloud maka bot akan berbelok 120 derajat.
8. Jika bot berada di sekitar asteroid field maka bot akan berbelok 120 derajat.
9. Jika semua kondisi sebelumnya tidak terpenuhi maka bot akan ,mencari makanan yang terdekat dengannya.

## Requirements

Program menggunakan maven sebagai build tools-nya sehingga

## Compile

Bot di-compile dengan menjalankan

``` java
mvn clean package
```
