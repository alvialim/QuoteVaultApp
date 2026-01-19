# Design Token Extraction Guide for Stitch Designs

## How to Extract Colors from Screenshots

### Option 1: Using Image Editor (Recommended)
1. Open screenshot in any image editor (Photoshop, Figma, Preview on Mac, etc.)
2. Use color picker tool
3. Note the hex code (e.g., #FF6B35)

### Option 2: Using Online Tools
1. Upload screenshot to https://imagecolorpicker.com/
2. Click on areas to get hex codes
3. Export color palette

### Option 3: Using Python Script
```bash
# Install PIL if needed: pip install Pillow
python3 extract_colors.py app/screenshots/home_screen.png
```

## What to Extract from Each Screenshot

### home_screen.png
- [ ] Background color (`background`)
- [ ] Primary button/action color (`primary`)
- [ ] Card background color (`surface`)
- [ ] Text color on background (`onBackground`)
- [ ] Text color on cards (`onSurface`)
- [ ] Quote card background
- [ ] Category badge colors

### login_screen.png
- [ ] Background color
- [ ] Text field colors (`outline`, `surfaceVariant`)
- [ ] Button colors (`primary`, `onPrimary`)
- [ ] Error color (`error`)

### favorite_screen.png
- [ ] Background color
- [ ] Card colors
- [ ] Favorite icon color
- [ ] Empty state colors

### collection_screen.png
- [ ] Background color
- [ ] Collection card colors
- [ ] Grid item colors

### settings_screen.png
- [ ] Background color
- [ ] Settings card colors
- [ ] Toggle/switch colors
- [ ] Section divider colors

## Material Design 3 Color Roles

Extract these from your screenshots and map them to MD3 roles:

| MD3 Color Role | Description | Extract From |
|----------------|-------------|--------------|
| `primary` | Main brand color | Buttons, icons, accents |
| `onPrimary` | Text/icon on primary | Button text |
| `primaryContainer` | Lighter primary | Button backgrounds |
| `onPrimaryContainer` | Text on container | Text in containers |
| `secondary` | Secondary accent | Secondary buttons |
| `background` | App background | Main screen background |
| `onBackground` | Text on background | Main text |
| `surface` | Card/sheet background | Cards, sheets |
| `onSurface` | Text on surface | Card text |
| `surfaceVariant` | Variant surface | Input fields |
| `outline` | Borders/dividers | Borders, dividers |
