# Thu Chi — quan ly thu chi ca nhan (offline 100%)

App Android 10+ (minSdk 29), native Java, DB Room offline.
Dung 1 minh, co no/vay ngay V1.0, backup .db + CSV sang may khac.

## V1.0 co gi

- **Nhap (< 3s)**: so tien to + Thu/Chi + luoi danh muc + vi + Luu.
  Tu them danh muc. Sua bang cach bam vao lich su, xoa bang giu lau.
- **Vi**: Tien mat / Ngan hang / Vi dien tu, so du tu tru/cong.
- **Lich su theo thang**: nhom theo ngay, loc ‹ ›, tong thu/chi/con lai.
- **Thong ke**: tron danh muc + cot thu/chi 6 thang (MPAndroidChart).
- **No/vay**: Cho vay / Di vay, tra 1 phan (sinh giao dich thu/chi),
  tu danh dau xong khi tra du, con no hien tong.
- **Backup**: menu 💾 → xuat `.db` (SAF) / nhap `.db` / xuat CSV.
  Sang may khac: copy file → nhap → mo lai app.

## Build APK (giong web-tts, khong can SDK local)

1. Tao repo moi tren github.com (dung tick Add README).
2. Tren LUNA36 (PowerShell tai `thu-chi`):
   ```
   git init
   git add .
   git -c user.name="tuyen36" -c user.email="tuyen36ptit13@gmail.com" commit -m "Thu Chi v1.0"
   git branch -M main
   git remote add origin <url-repo>
   git -c http.sslBackend=openssl push -u origin main
   ```
3. Repo → Actions → xanh (~5-10 phut) → Artifacts `app-debug` → cai.

## Cau truc

```
app/src/main/java/com/example/thuchi/
  MainActivity.java     tong quan thang + 4 nut
  AddActivity.java      nhap/sua + tra no nhanh (EXTRA_TX/EXTRA_DEBT)
  HistoryActivity.java  lich su thang (bam sua, giu xoa)
  StatsActivity.java    tron + cot 6 thang
  DebtActivity.java     no/vay + tra 1 phan
  BackupHelper.java     xuat/nhap .db + CSV (SAF)
  Money.java            vnd + parse + monthRange
  data/                 Room: Transaction/Category/Wallet/Debt/Dao/AppDb
```
