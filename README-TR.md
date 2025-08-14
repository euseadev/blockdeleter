# BlockDeleter Plugin

BlockDeleter, Minecraft sunucularında kullanılabilen bir eklentidir. Bu eklenti, WorldEdit ile seçilen bir bölgede yerleştirilen blokları 5 dakika sonra otomatik olarak siler.

## Gereksinimler

- Minecraft 1.16.5 - 1.21.* (1.16.5, 1.17.x, 1.18.x, 1.19.x, 1.20.x, 1.21.x sürümleri desteklenir)
- WorldEdit eklentisi
- Java 8 veya üzeri

## Kurulum

1. Eklenti JAR dosyasını sunucunuzun `plugins` klasörüne kopyalayın.
2. Sunucuyu yeniden başlatın veya `/reload` komutunu kullanın.

## Kullanım

1. WorldEdit eklentisi ile bir bölge seçin (örneğin, `//wand` komutu ile).
2. `/blockdeleter define` komutunu kullanarak seçilen bölgeyi tanımlayın.
3. Tanımlanan bölgeye bloklar yerleştirin.
4. Yerleştirilen bloklar 5 dakika sonra otomatik olarak silinecektir.

## İzinler

- `blockdeleter.use`: Eklentiyi kullanma izni (varsayılan olarak OP'lere verilir).

## Komutlar

- `/blockdeleter define`: WorldEdit ile seçilen bölgeyi tanımlar.

## Notlar

- Eklenti, WorldEdit eklentisine bağımlıdır ve WorldEdit olmadan çalışmaz.
- Tanımlanan bölgeler oyuncu bazlıdır, her oyuncu kendi bölgesini tanımlayabilir.
- Sunucu yeniden başlatıldığında tanımlanan bölgeler sıfırlanır.

## Uyumluluk

- Bu eklenti Minecraft 1.16.5 ile 1.21.* arasındaki tüm sürümlerde test edilmiş ve çalıştığı doğrulanmıştır.
- Özellikle 1.16.5, 1.19.4 ve 1.21 sürümlerinde test edilmiştir.
- Farklı Minecraft sürümleri için WorldEdit'in uyumlu sürümünü kullandığınızdan emin olun.