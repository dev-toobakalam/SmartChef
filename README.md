\# 🍳 Smart Chef



An Android app that helps you figure out what to cook with the ingredients you already have — search a recipe database by ingredients, or let AI generate a brand-new recipe on the spot. Supports English and Urdu.



\## 📸 Screenshots



\### 🏠 Home \& Search

<p align="center">

&#x20; <img src="Screenshots/home.png" width="200" />

&#x20; <img src="Screenshots/search.png" width="200" />

&#x20; <img src="Screenshots/matched.png" width="200" />

</p>

<p align="center">

&#x20; <em>Home Screen • Search Ingredients • Matched Recipes</em>

</p>



\---



\### 🤖 AI Recipe Generation

<p align="center">

&#x20; <img src="Screenshots/ai\_setup.png" width="200" />

&#x20; <img src="Screenshots/ai\_page.png" width="200" />

&#x20; <img src="Screenshots/spoonacular.png" width="200" />

</p>

<p align="center">

&#x20; <em>AI Setup • AI Generated Recipe • Spoonacular Fallback</em>

</p>



\---



\### ❤️ Favorites \& Shopping List

<p align="center">

&#x20; <img src="Screenshots/fav.png" width="200" />

&#x20; <img src="Screenshots/shopping.png" width="200" />

</p>

<p align="center">

&#x20; <em>Favorites • Shopping List</em>

</p>



\---



\### 📷 Camera \& Settings

<p align="center">

&#x20; <img src="Screenshots/camera.png" width="200" />

&#x20; <img src="Screenshots/settings.png" width="200" />

</p>

<p align="center">

&#x20; <em>Camera Ingredient Scanner • Settings (Language Toggle)</em>

</p>



\---



\### 🌐 Urdu Language Support

<p align="center">

&#x20; <img src="Screenshots/urdu.png" width="200" />

</p>

<p align="center">

&#x20; <em>App UI in Urdu</em>

</p>



\---



\## ✨ Features



\- \*\*Ingredient-based search\*\* — type up to 10 ingredients (or scan/speak them) and find matching recipes

\- \*\*AI-generated recipes\*\* — powered by Gemini via Firebase AI Logic; describe what you have, choose a cuisine and language, and get a full recipe with title, ingredients, and step-by-step instructions

\- \*\*Camera ingredient scanning\*\* — snap a photo of ingredients and extract text via on-device ML Kit OCR

\- \*\*Voice input\*\* — speak your ingredients instead of typing

\- \*\*Bilingual UI\*\* — full English and Urdu support, switchable from Settings

\- \*\*Favorites\*\* — save recipes locally with Room database

\- \*\*Shopping list\*\* — track items you need to buy

\- \*\*Recipe ratings\*\* — rate recipes, stored in Firestore

\- \*\*External recipe fallback\*\* — when no local match is found, search a global recipe API (Spoonacular) or generate one with AI instead



\## 🛠️ Tech Stack



| Layer | Technology |

|---|---|

| Language | Java |

| UI | Android Views, Material Components, `TabLayout` bottom navigation |

| AI | Firebase AI Logic (Gemini `gemini-2.5-flash`) |

| Database (local) | Room |

| Database (remote) | Firebase Firestore |

| Image loading | Glide |

| OCR | Google ML Kit Text Recognition |

| Networking | OkHttp, Gson |

| External recipe data | Spoonacular API |



\## 📱 Screens



\- \*\*Dashboard\*\* — animated splash/landing screen

\- \*\*Home\*\* — quick-glance welcome screen with a food image and quote

\- \*\*Search\*\* — ingredient input with camera/voice shortcuts, plus AI generation entry point

\- \*\*Recipe List\*\* — matched recipes ranked by ingredient overlap

\- \*\*Recipe Detail\*\* — full recipe view with favoriting and rating

\- \*\*Generate with AI\*\* — cuisine + language selection, AI-written recipe with structured Title/Ingredients/Steps sections

\- \*\*Favorites\*\* — saved recipes (offline, Room-backed)

\- \*\*Shopping List\*\* — simple checklist for groceries

\- \*\*Settings\*\* — language toggle (English/Urdu)



\## 🚀 Getting Started



\### Prerequisites

\- Android Studio (latest stable)

\- A Firebase project with \*\*AI Logic\*\* (Gemini Developer API) enabled

\- A \[Spoonacular API key](https://spoonacular.com/food-api) (free tier available)



\### Setup



1\. Clone the repo:

&#x20;  ```bash

&#x20;  git clone https://github.com/dev-toobakalam/smartchef.git

