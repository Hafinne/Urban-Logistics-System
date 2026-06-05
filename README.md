<img width="1919" height="1031" alt="image" src="https://github.com/user-attachments/assets/0952f25e-f7e2-4e04-b7cb-da813433842a" />
This project is an advanced urban logistics and package distribution system designed specifically for the city of Kayseri. Built completely with custom data structures (written from scratch without using standard Java Collections) and graph algorithms, it features a futuristic, anti-gravity inspired graphical user interface.

🛠️ Technical Features & Data Structures
The application strictly implements the following memory-managed data structures:

Master Registry (Singly Linked List - SLL): An immutable log for end-of-day auditing that appends every incoming package.

Intake Buffer (Doubly Linked List - DLL): A high-efficiency buffer utilizing head/tail pointers for O(1) package unloading from incoming trucks.

Standard Delivery (Queue): First-In, First-Out (FIFO) logic pipeline managing standard express shipments.

Truck Loading (Stack): Last-In, First-Out (LIFO) logic stack simulating a narrow truck cargo bay.

Address Directory (AVL Tree): A self-balancing binary search tree that optimizes neighborhood lookup times to O(log n).

City Map & Routing (Graph): An adjacency-list graph mapping out Kayseri's urban sectors.

Dijkstra's Algorithm: Computes the absolute shortest path for individual package delivery optimization.

Prim's Algorithm (MST): Calculates the Minimum Spanning Tree for overall city infrastructure and transport network efficiency.
-----------------------------------------------------------------------------------------------------------
Bu proje, Kayseri genelindeki lojistik ve dağıtım süreçlerini optimize etmek amacıyla geliştirilmiş bir yönetim sistemidir. Ham veri yapıları (Java hazır kütüphaneleri kullanılmadan sıfırdan yazılmıştır) ve gelişmiş graf algoritmaları entegre edilerek fütüristik bir kullanıcı arayüzü ile birleştirilmiştir.

### 🛠️ Teknik Özellikler & Veri Yapıları
Proje, ders isterlerine uygun olarak şu özel veri yapılarını barındırmaktadır:
* **Master Registry (Singly Linked List - SLL):** Depoya giren her paketin günlük kaydını tutan ve gün sonu denetimi sağlayan değiştirilemez günlük log sistemi.
* **Intake Buffer (Doubly Linked List - DLL):** Gelen tırlardan paketlerin O(1) karmaşıklıkla hızlıca indirilmesini sağlayan dinamik ara bellek.
* **Standard Delivery (Queue):** Standart gönderiler için ilk gelen ilk çıkar (FIFO) mantığıyla çalışan sevkiyat sırası.
* **Truck Loading (Stack):** Dar kargo alanlarını simüle eden, son gelen ilk çıkar (LIFO) mantığıyla çalışan yükleme yığını.
* **Address Directory (AVL Tree):** Mahalle ve adres sorgularını logaritmik sürede yapmak için kendi kendini dengeleyen arama ağacı.
* **City Map & Routing (Graph):** Kayseri mahallelerini ve mesafelerini tutan düğüm yapısı.
  * *Dijkstra Algoritması:* Bireysel paketler için en kısa ve en az maliyetli dağıtım rotasını hesaplar.
  * *Prim Algoritması (MST):* Tüm şehir dağıtım ağının genel verimliliğini ve altyapı planını optimize eder.

```bash
javac *.java
java MainApp
