# Stitch Design Updates - Color.kt

## 📋 What Changed

### **BEFORE:** Original Color.kt

The original file had hardcoded Material Design 3 colors without extraction markers.

**Key features:**
- Three accent color schemes (Purple, Blue, Green)
- Light and dark variants for each
- Category accent colors
- Quote card background colors

**Example:**
```kotlin
primary = Color(0xFF9C27B0),
background = Color(0xFFFFFBFE),
```

### **AFTER:** Stitch Design-Based Color.kt

Updated with clear extraction markers (TODO comments) indicating which screenshot to extract each color from.

**Key improvements:**
1. **Extraction Markers:** Every color now has a TODO comment indicating:
   - Which screenshot to extract from (e.g., `home_screen.png`)
   - Which UI element to extract from (e.g., "button color", "card background")
   - What the color is used for (e.g., "primary", "background")

2. **Organized by Source:**
   - Colors grouped by where they should be extracted from
   - Clear mapping between design elements and code

3. **Documentation:**
   - Comments explaining what each color role is
   - Reference to Material Design 3 color roles

**Example:**
```kotlin
// Primary: Extract from buttons, icons, accents in home_screen.png
primary = Color(0xFF9C27B0), // TODO: Extract from Stitch - button color
background = Color(0xFFFFFBFE), // TODO: Extract from Stitch - main background
```

## 🎨 Extraction Instructions

### Step 1: Open Screenshots
Open `app/screenshots/home_screen.png` in any image editor or design tool.

### Step 2: Extract Primary Colors
1. Find the primary button/action (e.g., "Login" button, heart icon)
2. Use color picker to get hex code
3. Replace `Color(0xFF9C27B0)` with extracted value

### Step 3: Extract Background Colors
1. Find the main background color
2. Extract hex code
3. Replace background values

### Step 4: Extract Card Colors
1. Find quote card backgrounds
2. Extract card surface color
3. Extract text color on cards

### Step 5: Extract Category Colors
1. Find category badges/chips
2. Extract each category's color:
   - Motivation badge
   - Love badge
   - Success badge
   - etc.

## 📸 Screenshot Mapping

| Screenshot | Extract These Colors |
|------------|---------------------|
| `home_screen.png` | Primary, background, surface, quote card colors, category badges |
| `login_screen.png` | Primary button, input field colors, error colors |
| `favorite_screen.png` | Favorite icon color, card colors, background |
| `collection_screen.png` | Collection card colors, grid background |
| `settings_screen.png` | Settings card colors, toggle colors, divider colors |

## ✅ Next Steps

1. **Extract Colors:** Use the TODO comments to extract all colors from screenshots
2. **Update Color.kt:** Replace placeholder values with extracted hex codes
3. **Test:** Build and run app to verify colors match Stitch designs
4. **Refine:** Adjust colors if needed to match exact Stitch specifications

## 📝 Notes

- All colors should be extracted from the actual Stitch design screenshots
- Keep Material Design 3 color roles (primary, secondary, surface, etc.)
- Ensure sufficient contrast for accessibility (use Material Design contrast ratios)
- Test in both light and dark modes if dark mode screenshots are available
