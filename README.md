# SaleMath 🏷️🔢

**SaleMath** is a modern, privacy-focused, offline-first Android application designed to handle sequential multi-step discount calculations accurately. Whether shopping during promotional sales or analyzing tiered discount structures, SaleMath calculates exact price breakdowns, effective discount percentages, and total monetary savings.

---

## ✨ Features

- **Sequential Multi-Discount Engine**: Apply multiple discounts sequentially (e.g., 20% + 10%) with mathematical precision. SaleMath calculates each step accurately rather than simply adding percentage values.
- **Stacked Discounts (Up to 10)**: Dynamic discount rows allowing users to add, edit, or remove up to 10 sequential discount steps.
- **Comprehensive Price Breakdown**:
  - Original price & final calculated price
  - Total monetary savings
  - True effective discount percentage
  - Step-by-step discount breakdown table & formula view
- **Quantity Calculations**: Calculate total original cost, total final cost, and total savings for multiple items simultaneously.
- **Custom Price Rounding**: Optional final price rounding with configurable increments (e.g., nearest $0.05, $0.50, $1.00) and directions (*Nearest*, *Up / Ceiling*, *Down / Floor*).
- **History & Favorites**:
  - Automatically saves calculation history (up to 50 calculations)
  - Toggle calculation favorites with priority sorting
  - Search, filter, or clear calculation history
- **Export & Share**:
  - Quick-copy final price or fully formatted calculation breakdown
  - Native offline Android share sheet integration
- **Multi-Currency Support**: Supports various currencies including USD ($), EUR (€), GBP (£), PKR (Rs), INR (₹), CAD ($), AUD ($), JPY (¥), SAR (SR), AED (AED), and more.
- **Privacy & Offline First**: 100% offline operation with zero network tracking or external API dependencies.

---

## 🛠️ Tech Stack & Architecture

SaleMath is built using modern Android development practices and Jetpack libraries:

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Layer Separation
- **Navigation**: Jetpack Navigation Compose
- **Persistence**: Jetpack DataStore Preferences & JSON serialization
- **Asynchronous Execution**: Kotlin Coroutines & StateFlow
- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target / Compile SDK**: API 37

---

## 📂 Project Structure

```text
com.example.salemath
├── calculator/       # Core calculation engine (SaleMathCalculator)
├── data/             # Data persistence (PreferencesManager & JSON mapping)
├── model/            # Data models (SaleMathInput, SaleMathResult, DiscountStep, etc.)
├── ui/
│   ├── components/   # Reusable Compose UI components (ResultCards, FormulaView, etc.)
│   ├── navigation/   # Jetpack Navigation graphs and screen definitions
│   ├── screens/      # App screens (Calculator, History, Settings, About, Splash)
│   └── theme/        # Material 3 colors, typography, and theme definitions
├── util/             # Utility helpers (MoneyFormatter, HapticHelper)
└── viewmodel/        # SaleMathViewModel managing UI state and persistence
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java Development Kit (JDK) 11 or higher.
- **Android SDK**: API Level 37 SDK installed.

### Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/SaleMath.git
   cd SaleMath
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open**, then navigate to the cloned `SaleMath` directory.

3. **Build the Project**:
   Build the project using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   Execute the suite of unit tests verifying calculator math and rounding logic:
   ```bash
   ./gradlew test
   ```

5. **Run on Device / Emulator**:
   Deploy the app directly to a connected Android device or emulator running API 24+.

---

## 🧪 Testing

Unit tests for the calculation logic are located at:
`app/src/test/java/com/example/salemath/SaleMathCalculatorTest.kt`

Tests cover:
- Single & sequential discount math
- Edge cases (0% discount, 100% discount, 0 price)
- Multi-quantity totals
- Custom increment rounding (HALF_UP, CEILING, FLOOR)
- Effective discount percentage verification

---

## 📄 License

This project is open-source and available under the terms of the [MIT License](LICENSE).
