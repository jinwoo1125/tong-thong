# Noise 백엔드 API 스펙 (G1)

Base URL: `http://<host>:3000/`  
인증: JWT Bearer Token (`Authorization: Bearer <token>`)

---

## DB 스키마

### users
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | |
| email | TEXT UNIQUE NOT NULL | |
| password_hash | TEXT NOT NULL | bcrypt |
| nickname | TEXT UNIQUE NOT NULL | 자동 생성 or 입력 |
| created_at | DATETIME DEFAULT NOW | |

### artist_profiles
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| user_id | INTEGER FK → users.id | UNIQUE |
| debut_date | TEXT | YYYY-MM-DD |
| artist_type | TEXT | 솔로/밴드/DJ 등 |
| agency | TEXT | |
| bio | TEXT | |
| avatar_url | TEXT | 프로필 이미지 경로 |

### genres
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | 1~12 고정 |
| name | TEXT | 팝/힙합/R&B … |

### songs
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | |
| uploader_id | INTEGER FK → users.id | |
| title | TEXT | 실제 제목 (블라인드 공개 전 숨김) |
| file_path | TEXT | 음원 파일 경로 |
| cover_path | TEXT NULLABLE | 커버 이미지 경로 |
| genre_id | INTEGER FK → genres.id NULLABLE | |
| description | TEXT NULLABLE | |
| lyrics | TEXT NULLABLE | |
| play_count | INTEGER DEFAULT 0 | |
| created_at | DATETIME DEFAULT NOW | |

응답에 `uploader` (nickname), `genre` (name), `like_count`, `comment_count` 집계 포함.

### likes
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| user_id | INTEGER FK → users.id | |
| song_id | INTEGER FK → songs.id | |
| created_at | DATETIME | |
UNIQUE(user_id, song_id)

### comments
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| user_id | INTEGER FK → users.id | |
| song_id | INTEGER FK → songs.id | |
| content | TEXT NOT NULL | |
| created_at | DATETIME DEFAULT NOW | |

### saves (내 보관함)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| user_id | INTEGER FK → users.id | |
| song_id | INTEGER FK → songs.id | |
| custom_title | TEXT | 사용자 지정 제목 |
| created_at | DATETIME DEFAULT NOW | |
UNIQUE(user_id, song_id)

### ratings (별점)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| user_id | INTEGER FK → users.id | |
| song_id | INTEGER FK → songs.id | |
| score | INTEGER CHECK(1~5) | |
| created_at | DATETIME | |
UNIQUE(user_id, song_id)

### follows
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | INTEGER PK | |
| follower_id | INTEGER FK → users.id | 팔로우 하는 사람 |
| following_id | INTEGER FK → users.id | 팔로우 당하는 아티스트 |
| created_at | DATETIME | |
UNIQUE(follower_id, following_id)

---

## API 엔드포인트

### 인증

#### POST /api/auth/register
```json
Request:  { "email": "string", "password": "string", "genres": [1, 3] }
Response: { "token": "jwt", "nickname": "string" }
```
- 닉네임 자동 생성 (예: `user_랜덤4자리`)
- 비밀번호 bcrypt 해시 저장

#### POST /api/auth/login
```json
Request:  { "email": "string", "password": "string" }
Response: { "token": "jwt", "nickname": "string" }
```

---

### 음악

#### GET /api/songs
```
Query: genre_id (int, optional), page (int, default 1), limit (int, default 20)
Response: SongResponse[]
```

#### GET /api/songs/:id
```
Response: SongResponse
```

SongResponse 형식:
```json
{
  "id": 1,
  "title": "노래제목",
  "file_path": "uploads/songs/xxx.mp3",
  "cover_path": "uploads/covers/xxx.jpg",
  "play_count": 1234,
  "created_at": "2024-01-01T00:00:00Z",
  "uploader": "닉네임",
  "genre_id": 1,
  "genre": "팝",
  "like_count": 56,
  "comment_count": 12
}
```

#### POST /api/songs (multipart/form-data) 🔒
```
Parts: song (file), cover (file, optional)
Fields: title, genre_id, description, lyrics
Response: { "message": "업로드 성공" }
```
- 파일 저장 후 경로를 DB에 기록
- play_count = 0으로 초기화

#### GET /api/songs/:id/stream
- 음원 파일 스트리밍 (Range Request 지원 권장)
- `Content-Type: audio/mpeg`

#### DELETE /api/songs/:id 🔒
- 본인 곡만 삭제 가능
```json
Response: { "message": "삭제 완료" }
```

---

### 좋아요

#### POST /api/songs/:id/like 🔒
- 토글 방식 (없으면 추가, 있으면 제거)
```json
Response: { "liked": true }
```

#### GET /api/songs/:id/like 🔒
```json
Response: { "liked": false }
```

---

### 댓글

#### GET /api/songs/:id/comments
```
Query: page (int, default 1)
Response: [{ "id": 1, "content": "string", "created_at": "ISO8601", "nickname": "string" }]
```

#### POST /api/songs/:id/comments 🔒
```json
Request:  { "content": "댓글 내용" }
Response: { "message": "댓글 등록 완료" }
```

#### DELETE /api/songs/:id/comments/:commentId 🔒
- 본인 댓글만 삭제 가능
```json
Response: { "message": "삭제 완료" }
```

---

### 보관함 (Saves)

#### POST /api/songs/:id/save 🔒
```json
Request:  { "custom_title": "저장 제목" }
Response: { "message": "저장 완료" }
```

#### DELETE /api/songs/:id/save 🔒
```json
Response: { "message": "삭제 완료" }
```

#### GET /api/saves 🔒
```json
Response: [
  {
    "id": 1,
    "custom_title": "내가 붙인 이름",
    "created_at": "ISO8601",
    "song_id": 42,
    "original_title": "원제",
    "cover_path": "uploads/covers/xxx.jpg",
    "uploader": "닉네임",
    "genre": "팝"
  }
]
```

---

### 별점

#### POST /api/songs/:id/rating 🔒
```json
Request:  { "score": 4 }
Response: { "avg_score": "3.8", "count": 25, "my_score": 4 }
```
- 이미 별점 있으면 업데이트 (UPSERT)

#### GET /api/songs/:id/rating 🔒
```json
Response: { "avg_score": "3.8", "count": 25, "my_score": 4 }
```
- 미인증 또는 미평가시 `my_score: null`

---

### 차트

#### GET /api/charts
```
Query: period (daily|weekly|monthly|yearly, default daily), genre_id (optional)
Response: SongResponse[] (정렬: play_count DESC, 최대 100개)
```

period별 집계 기간:
- daily: 최근 24시간
- weekly: 최근 7일
- monthly: 최근 30일
- yearly: 최근 365일

---

### 아티스트 프로필

#### PUT /api/profile 🔒
```json
Request:  { "debut_date": "2020-01-01", "artist_type": "솔로", "agency": "인디", "bio": "소개" }
Response: { "message": "저장 완료" }
```
- artist_profiles UPSERT (user_id 기준)

#### GET /api/profile 🔒 (내 프로필)
```json
Response: {
  "nickname": "string",
  "debut_date": "string|null",
  "artist_type": "string|null",
  "agency": "string|null",
  "bio": "string|null",
  "avatar_url": "string|null"
}
```

#### GET /api/profile/:nickname (공개 프로필)
- 인증 불필요
- 동일 응답 형식

---

### 팔로우

#### POST /api/follow/:nickname 🔒
- 해당 nickname의 유저를 팔로우
```json
Response: { "following": true }
```

#### DELETE /api/follow/:nickname 🔒
```json
Response: { "following": false }
```

#### GET /api/follow/:nickname 🔒
```json
Response: { "following": true }
```

---

### 아티스트 통계

#### GET /api/stats/:nickname
- 인증 불필요 (공개 통계)
```json
Response: {
  "upload_count": 12,
  "total_play_count": 32000,
  "follower_count": 1248
}
```

---

## 인증 미들웨어

모든 🔒 엔드포인트에 적용:

```js
// Express 예시
const auth = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1]
  if (!token) return res.status(401).json({ message: '인증 필요' })
  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET)
    next()
  } catch {
    res.status(401).json({ message: '토큰 만료 또는 무효' })
  }
}
```

---

## 파일 업로드 설정 (multer 예시)

```js
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = file.fieldname === 'song' ? 'uploads/songs' : 'uploads/covers'
    cb(null, dir)
  },
  filename: (req, file, cb) => {
    cb(null, `${Date.now()}_${file.originalname}`)
  }
})
const upload = multer({ storage, limits: { fileSize: 50 * 1024 * 1024 } })
```

정적 파일 서빙:
```js
app.use('/uploads', express.static('uploads'))
```

커버 이미지 URL: `http://<host>:3000/uploads/covers/<filename>`  
(앱 내 `RetrofitClient.coverUrl(cover_path)` 가 이 형식을 그대로 조합)

---

## play_count 증가 시점

- `GET /api/songs/:id/stream` 최초 요청 시 또는 일정 재생 시간(30초) 이후 카운트
- 추천: 별도 `POST /api/songs/:id/play` 엔드포인트로 앱에서 명시적 호출
  ```json
  Response: { "message": "ok" }
  ```
  현재 앱 코드는 stream URL 직접 호출 방식 사용 중이므로 stream 핸들러에서 카운트하는 것이 가장 간단.
