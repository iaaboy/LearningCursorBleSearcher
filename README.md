# BLE 기기 스캐너

주변의 BLE(Bluetooth Low Energy) 기기들을 스캔하여 실시간으로 목록을 보여주는 Android 애플리케이션입니다.

## 주요 기능

- **실시간 BLE 스캔**: 주변의 모든 BLE 기기 감지
- **기기 정보 표시**: 기기명, MAC 주소, 신호 강도(RSSI), 마지막 감지 시간
- **신호 강도 시각화**: 색상으로 신호 강도 표시 (Excellent/Good/Fair/Poor/Very Poor)
- **권한 관리**: 런타임 권한 요청 및 블루투스 활성화
- **한국어 UI**: 완전한 한국어 인터페이스

## 프로젝트 구조

```
MyAndroidApp/
├── app/
│   ├── build.gradle                 # 앱 모듈 설정
│   ├── proguard-rules.pro          # 코드 난독화 규칙
│   └── src/main/
│       ├── AndroidManifest.xml     # 앱 매니페스트 (BLE 권한 포함)
│       ├── java/com/example/myandroidapp/
│       │   ├── MainActivity.kt     # 메인 액티비티
│       │   ├── BleDevice.kt        # BLE 기기 데이터 모델
│       │   ├── BleScanner.kt       # BLE 스캔 로직
│       │   ├── DeviceAdapter.kt    # RecyclerView 어댑터
│       │   └── PermissionHelper.kt # 권한 관리 헬퍼
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml    # 메인 UI 레이아웃
│           ├── values/
│           │   ├── colors.xml           # 색상 정의
│           │   ├── strings.xml          # 한국어 문자열
│           │   └── themes.xml           # 앱 테마
│           └── xml/
│               ├── backup_rules.xml     # 백업 설정
│               └── data_extraction_rules.xml
├── build.gradle                    # 루트 프로젝트 설정
├── settings.gradle                 # 프로젝트 설정
├── gradle.properties               # Gradle 속성
├── gradle/wrapper/                 # Gradle 래퍼 파일
├── gradlew                         # Gradle 래퍼 스크립트
└── README.md                       # 프로젝트 문서
```

## 요구사항

- Android Studio Arctic Fox 이상
- Android SDK 24+ (API level 24)
- Kotlin 1.9.10+
- Gradle 8.0+
- BLE 지원 기기
- 위치 권한 (BLE 스캔에 필요)

## 설치 및 실행

1. **Android Studio에서 열기:**
   - Android Studio 실행
   - "Open an existing Android Studio project" 선택
   - `MyAndroidApp` 폴더로 이동하여 선택

2. **프로젝트 동기화:**
   - Android Studio가 자동으로 Gradle과 프로젝트를 동기화
   - 동기화 완료까지 대기

3. **앱 실행:**
   - Android 기기 연결 또는 에뮬레이터 시작
   - "Run" 버튼 (녹색 재생 아이콘) 클릭
   - 대상 기기 선택 후 "OK" 클릭

## 명령줄에서 빌드

명령줄에서 빌드하려면:

```bash
# 프로젝트 디렉토리로 이동
cd MyAndroidApp

# 프로젝트 빌드
./gradlew build

# 연결된 기기에 설치
./gradlew installDebug

# 테스트 실행
./gradlew test
```

## 앱 기능

### 주요 화면 구성
- **제목**: "BLE 기기 스캐너"
- **기기 수 표시**: 발견된 기기 개수
- **스캔 버튼**: 스캔 시작/중지 토글
- **클리어 버튼**: 발견된 기기 목록 초기화
- **기기 목록**: RecyclerView로 기기 정보 표시

### 기기 정보 표시
- **기기명**: 기기의 이름 (Unknown Device if null)
- **MAC 주소**: 기기의 고유 주소
- **신호 강도**: RSSI 값과 색상 표시
- **마지막 감지 시간**: 기기를 마지막으로 감지한 시간

### 권한 관리
- **위치 권한**: BLE 스캔에 필수
- **블루투스 권한**: Android 12+ 에서 필요
- **블루투스 활성화**: 자동 블루투스 활성화 요청

## 사용된 기술

- **Kotlin**: 주요 프로그래밍 언어
- **AndroidX**: 최신 Android 지원 라이브러리
- **Material Design 3**: 최신 UI 컴포넌트
- **View Binding**: 타입 안전한 뷰 접근
- **ConstraintLayout**: 유연한 레이아웃 시스템
- **RecyclerView**: 효율적인 리스트 표시
- **BLE API**: Bluetooth Low Energy 스캔

## 커스터마이징

다음과 같이 앱을 쉽게 커스터마이징할 수 있습니다:

1. **색상 변경**: `app/src/main/res/values/colors.xml` 편집
2. **텍스트 수정**: `app/src/main/res/values/strings.xml` 편집
3. **UI 업데이트**: `app/src/main/res/layout/activity_main.xml` 편집
4. **기능 추가**: `MainActivity.kt` 또는 관련 클래스 수정

## 라이선스

이 프로젝트는 MIT 라이선스 하에 오픈소스로 제공됩니다. 