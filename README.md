# BeatPlay — লাইভ টিভি + মুভি স্ট্রিমিং অ্যাপ

AndroidIDE-তে চলবে এমন একটি Android অ্যাপ। Firebase Realtime Database-এ চ্যানেল/মুভির স্ট্রিম লিংক (m3u8, mp4, dash) রাখা হয় এবং অ্যাপ সেই লিংকগুলো এক্সো-প্লেয়ার দিয়ে চালায়। অ্যাপের ভিতরেই একটি লুকানো অ্যাডমিন প্যানেল আছে — গিয়ার আইকনে ট্যাপ করে পাসওয়ার্ড দিলে সব কনটেন্ট ম্যানেজ করা যায়।

## ফিচার

**ইউজার সাইড**
- **হোমপেইজ** — স্ক্রলিং ব্যানার ক্যারোসেল (অটো-স্ক্রল + ডট ইন্ডিকেটর), ঘোষণা/নোটিফিকেশন সেকশন, "লাইভ চ্যানেল" ও "নতুন মুভি" হরাইজন্টাল সেকশন
- **বটম নেভিগেশন** — হোম / লাইভ টিভি / মুভি (Material 3 আইকন সহ)
- **ঘোষণা** — টুলবারের বেল আইকনে ট্যাপ করলে সব ঘোষণার তালিকা
- **লাইভ টিভি ট্যাব** — Firebase থেকে চ্যানেল লিস্ট, ট্যাপ করলেই প্লে
- **মুভি ট্যাব** — থাম্বনেইলসহ মুভি লিস্ট, ট্যাপ করলেই প্লে
- HLS (m3u8), mp4, DASH সাপোর্ট (Media3 ExoPlayer), ফুলস্ক্রিন প্লেয়ার

**অ্যাডমিন সাইড (লুকানো)**
- টুলবারের গিয়ার আইকন → পাসওয়ার্ড (SHA-256) → অ্যাডমিন প্যানেল
- ৪টি ট্যাব: **চ্যানেল**, **মুভি**, **ব্যানার**, **ঘোষণা**
- **সিংগল আপলোড** — ডায়ালগ দিয়ে একটি করে চ্যানেল/মুভি যোগ
- **বাল্ক আপলোড** — একসাথে অনেকগুলো চ্যানেল/মুভি (`নাম | লিংক | ...`) পেস্ট করে যোগ
- **প্লে লিস্ট ইমপোর্ট** — M3U/M3U8 প্লে লিস্টের URL বা টেক্সট থেকে চ্যানেল অটো-ইমপোর্ট (লোগো + ক্যাটাগরি সহ)
- **ব্যানার CRUD** — হোমপেইজের ব্যানার যোগ/এডিট/ডিলিট, টার্গেট চ্যানেল/মুভি সিলেক্ট করা যায়
- **ঘোষণা CRUD** — নোটিফিকেশন যোগ/এডিট/ডিলিট (সময়সহ, নতুনটি আগে দেখায়)

## প্রজেক্ট স্ট্রাকচার

```
app/
  src/main/
    java/com/beat/play/
      MainActivity.java          # বটম নেভ + ৩ ট্যাব (হোম/লাইভ/মুভি), বেল + গিয়ার আইকন
      PlayerActivity.java        # ExoPlayer ভিডিও প্লেয়ার
      AnnouncementsActivity.java # সব ঘোষণার তালিকা
      LoginActivity.java         # অ্যাডমিন পাসওয়ার্ড প্রম্পট
      AdminActivity.java         # অ্যাডমিন প্যানেল (৪ ট্যাব)
      adapter/                   # RecyclerView অ্যাডাপ্টার
      data/DataStore.java        # Firebase রেফারেন্স
      model/                     # Channel, Movie, Banner, Announcement
      ui/                        # ফ্রাগমেন্ট (হোম + লিস্ট + অ্যাডমিন)
      util/                      # SHA256, PlaylistParser (M3U), HttpUtil
    res/layout/                  # সব লেআউট
```

## সেটআপ (ধাপে ধাপে)

### ১. Firebase প্রজেক্ট তৈরি

1. https://console.firebase.google.com-এ গিয়ে "Add project"
2. প্রজেক্টের নাম দিন (যেমন `livetv`) এবং Create
3. Project Settings → Your apps → **Android** আইকনে ক্লিক করুন
4. Package name-এ লিখুন: `com.beat.play` (build.gradle-এর applicationId-এর সাথে মিলতে হবে)
5. অ্যাপ রেজিস্টার করুন → **google-services.json** ফাইলটি ডাউনলোড করুন
6. ফাইলটি `/workspace/app/` ফোল্ডারে কপি করুন (অর্থাৎ `app/google-services.json`)

### ২. Realtime Database চালু করা

1. Firebase Console → Build → **Realtime Database** → Create Database
2. লোকেশন নির্বাচন করুন, mode-এ **Test mode** (ডেভেলপমেন্টের জন্য) → Enable
3. Rules ট্যাব → নিচের রুলসগুলো পেস্ট করুন → Publish

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

> **সতর্কতা:** পাসওয়ার্ড-প্রম্পট পদ্ধতিতে `.write: true` মানে নিয়ম-ভিত্তিক কোনো সুরক্ষা নেই — যেকেউ লেখার অ্যাক্সেস পায়। প্রোডাকশনে Firebase Authentication দিয়ে `"auth != null"` বা কাস্টম রুলস ব্যবহার করলে সেটা নিরাপদ হবে। বিস্তারিত: নিচের "নিরাপত্তা নোট"।

### ৩. অ্যাডমিন পাসওয়ার্ড সেট করা

অ্যাপটি `settings/adminPasswordHash` নামক নোডে SHA-256 হ্যাশ যাচাই করে। হ্যাশটি বের করুন:

```bash
python3 -c "import hashlib; print(hashlib.sha256('আপনার_পাসওয়ার্ড'.encode()).hexdigest())"
```

তারপর Firebase Console → Realtime Database → ডেটা ট্যাব থেকে **+** চেপে:

```
settings (অবজেক্ট)
  └── adminPasswordHash: "উপরের আউটপুটটি"
```

এখন অ্যাপে গিয়ার আইকনে ট্যাপ করে ওই পাসওয়ার্ড দিলে অ্যাডমিন প্যানেল খুলবে।

### ৪. AndroidIDE-তে প্রজেক্ট খোলা

1. AndroidIDE → Open Project → `/workspace` (যে ফোল্ডারে এই ফাইলগুলো আছে) সিলেক্ট করুন
2. AndroidIDE নিজে Gradle sync করবে (ইন্টারনেট থাকতে হবে)
3. Sync সফল হলে **Run** চেপে অ্যাপ ইনস্টল করুন

> **গুরুত্বপূর্ণ:** `app/google-services.json` না থাকলে build ব্যর্থ হবে (google-services প্লাগইন error দেবে)। প্রথমে ধাপ ১ সম্পূর্ণ করুন।

## ডেটা স্ট্রাকচার

Firebase Realtime Database-এ এই রকম দেখাবে:

```json
{
  "settings": {
    "adminPasswordHash": "e0b1b3a3d9a13d0a4c... "
  },
  "channels": {
    "-Nx1abc": {
      "name": "BD News 24",
      "url": "https://example.com/live/bd24.m3u8",
      "logo": "https://example.com/logo.png",
      "category": "সংবাদ"
    }
  },
  "movies": {
    "-Nx2xyz": {
      "title": "নাম",
      "url": "https://example.com/movie.m3u8",
      "thumbnail": "https://example.com/poster.jpg",
      "description": "বর্ণনা",
      "year": "2024",
      "category": "অ্যাকশন"
    }
  },
  "banners": {
    "-Nx3abc": {
      "title": "প্রিমিয়ার মুভি",
      "image": "https://example.com/banner.jpg",
      "targetTitle": "মুভির নাম",
      "targetUrl": "https://example.com/movie.m3u8"
    }
  },
  "notifications": {
    "-Nx4xyz": {
      "title": "নতুন চ্যানেল যুক্ত হয়েছে",
      "message": "বিস্তারিত",
      "timestamp": 1700000000000
    }
  }
}
```

অ্যাডমিন প্যানেল থেকে চ্যানেল/মুভি/ব্যানার/ঘোষণা যোগ, এডিট ও ডিলিট করতে পারবেন — ম্যানুয়ালি Firebase-এ লেখার দরকার নেই (প্রথমবার পাসওয়ার্ড হ্যাশটা ছাড়া)।

## বাল্ক আপলোড ও প্লে লিস্ট ফরম্যাট

**চ্যানেল বাল্ক** (প্রতিটি লাইনে):

```
নাম | লিংক | ক্যাটাগরি | লোগো URL
BD News 24 | https://example.com/live.m3u8 | সংবাদ | https://example.com/logo.png
```

**মুভি বাল্ক**:

```
নাম | লিংক | থাম্বনেইল | সাল | ক্যাটাগরি
```

**প্লে লিস্ট (M3U/M3U8):** অ্যাডমিন → চ্যানেল ট্যাব → FAB → "প্লে লিস্ট ইমপোর্ট" → প্লে লিস্টের URL দিন বা টেক্সট পেস্ট করুন। `#EXTINF` লাইন থেকে লোগো ও ক্যাটাগরি (group-title) অটো-পার্স হয়ে যায়।

## নিরাপত্তা নোট

- **পাসওয়ার্ড প্রম্পট** শুধু UI-তে গেট দেয়; ডেটাবেসের রুলস যদি `.write: true` থাকে তাহলে অ্যাপের ডেটা যেকেউ বদলাতে পারবে।
- আরও নিরাপদ করতে চাইলে:
  1. Firebase Authentication (ইমেইল/পাসওয়ার্ড) চালু করুন এবং অ্যাপে `FirebaseAuth` দিয়ে অ্যাডমিন লগইন করুন
  2. রুলসে লিখুন: `"channels": { ".read": true, ".write": "auth != null" }`
  3. অ্যাডমিনদের জন্য `rules`/`isAdmin`-ভিত্তিক রুলস ব্যবহার করুন
- স্ট্রিম লিংকের কপিরাইট/লাইসেন্স যাচাই করে নিজ দায়িত্বে ব্যবহার করবেন।

## কাস্টমাইজেশন

- **অ্যাপের নাম:** `app/src/main/res/values/strings.xml`-এ `app_name`
- **প্যাকেজ/applicationId:** `app/build.gradle`-এ পরিবর্তন করুন এবং Manifest-এ সব `.Activity` রেফারেন্স মিলে যাবে (namespace একই থাকবে)
- **রঙ:** `app/src/main/res/values/colors.xml`
- **লোগো:** `app/src/main/res/drawable/ic_tv.xml` প্রতিস্থাপন করুন
- Realtime Database যদি আঞ্চলিক URL-এ থাকে (`xxx-default-rtdb.firebaseio.com` ছাড়া), তাহলে `DataStore.java`-তে `FirebaseDatabase.getInstance("https://.../")` ব্যবহার করুন

## সমস্যা সমাধান

| সমস্যা | সমাধান |
|---|---|
| Build error: google-services.json missing | Firebase থেকে ফাইলটি ডাউনলোড করে `app/` ফোল্ডারে রাখুন |
| অ্যাপে কোনো চ্যানেল দেখা যায় না | Realtime Database-এ ডেটা আছে কি না, এবং রুলস `.read: true` আছে কি না দেখুন |
| ভিডিও প্লে হয় না | লিংকটি মোবাইল ব্রাউজারে খোলে কি না পরীক্ষা করুন; কিছু m3u8 চেইন CORS ব্লক করে |
| "অ্যাডমিন পাসওয়ার্ড সেট করা নেই" | `settings/adminPasswordHash` সঠিকভাবে আছে কি না দেখুন |
