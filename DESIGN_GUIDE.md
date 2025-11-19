# 🎨 ClubCI App - Visual Design Guide

## Color Palette

### Primary Colors

```
Purple Gradient:
├─ Start:  #7C3AED  ■■■■■
├─ Middle: #A855F7  ■■■■■
└─ End:    #EC4899  ■■■■■

Secondary Colors:
├─ Teal:   #14B8A6  ■■■■■
├─ Green:  #10B981  ■■■■■
└─ Cyan:   #06B6D4  ■■■■■
```

### Usage Examples

**Backgrounds:**

- Login/Register/Splash: Full gradient (#7C3AED → #A855F7 → #EC4899)
- App Bar: Gradient background
- Cards: White with subtle gradient border

**Buttons:**

- Primary: Gradient purple to violet
- Text: White (#FFFFFF)
- Height: 60dp, Corner: 16dp

**Text:**

- Headings: #111827 (dark) / #F9FAFB (on gradient)
- Body: #6B7280
- Links: #7C3AED

---

## Typography Scale

```
Headline:  32-40sp, Bold, 0.05 letter spacing
Title:     22-28sp, Bold, 0.02 letter spacing
Subtitle:  18-20sp, Bold
Body:      15-16sp, Regular
Caption:   12-14sp, Regular
```

---

## Spacing System

```
Micro:   4dp   (badges, chips)
Small:   8dp   (icon margins)
Medium:  16dp  (content padding)
Large:   24dp  (section spacing)
XLarge:  32dp  (screen margins)
```

---

## Elevation Scale

```
Resting:   0dp  (flat surfaces)
Low:       2dp  (subtle cards)
Medium:    6dp  (standard cards)
High:      8dp  (important cards)
Floating:  12dp (FAB)
Modal:     16dp (dialogs, elevated cards)
```

---

## Border Radius

```
Small:   8dp   (chips, small badges)
Medium:  16dp  (buttons, inputs)
Large:   20dp  (standard cards)
XLarge:  24dp  (feature cards)
Circle:  50%   (avatars, FAB)
```

---

## Component Specifications

### Login Card

```
Width:     match_parent (with 28dp margins)
Padding:   32dp
Corner:    24dp
Elevation: 16dp
Background: White
Shadow:    Subtle gradient shadow
```

### Event Card

```
Width:     match_parent (with 16dp margins)
Corner:    24dp
Elevation: 8dp
Header:    Gradient background (gradient_card.xml)
Border:    1dp with #10A855F7 (10% opacity purple)
Content:   20dp padding
```

### Input Fields

```
Height:     56dp (wrap_content)
Corner:     16dp
Border:     2dp (focused), 1dp (unfocused)
Icon Size:  22-24dp
Padding:    16dp horizontal, 12dp vertical
```

### Buttons

```
Height:     60dp
Corner:     16dp
Padding:    16dp vertical
Text:       18sp, Bold
Background: Gradient (button_gradient.xml)
Elevation:  8dp
```

### Profile Avatar

```
Size:       110dp outer card
            70dp inner icon
Corner:     55dp (circular)
Elevation:  12dp
Background: White
```

---

## Screen Layouts

### Login Screen

```
┌─────────────────────────────┐
│ ▓▓▓ Gradient Background ▓▓▓ │ Decorative circle (top-right)
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│                             │
│        ┌───────┐            │ Logo (100dp card)
│        │  [📱]  │           │
│        └───────┘            │
│                             │
│         ClubCI              │ App name (32sp)
│    Manage Your Events       │ Tagline (16sp)
│                             │
│  ┌─────────────────────┐   │
│  │                     │   │ White card container
│  │  Welcome Back!      │   │ (24dp corners, 16dp elevation)
│  │  Sign in...         │   │
│  │                     │   │
│  │  [👤 Username]      │   │ Input fields
│  │  [🔒 Password]      │   │ (16dp corners)
│  │                     │   │
│  │  ┌──────LOGIN──────┐│   │ Gradient button
│  │  └─────────────────┘│   │ (60dp height)
│  │                     │   │
│  └─────────────────────┘   │
│                             │
│  Don't have account? Sign Up│ Link text
└─────────────────────────────┘
```

### Event Card

```
┌───────────────────────────────┐
│ ╔══════════════════════════╗  │ Gradient header
│ ║  Tech Fest 2024    [TECH]║  │ (gradient_card.xml)
│ ╚══════════════════════════╝  │
│                               │
│  ┌─────────────────────────┐  │ Date/Venue card
│  │ 📅 Dec 25, 2024         │  │
│  │ ──────────────────────  │  │
│  │ 📍 Main Auditorium      │  │
│  └─────────────────────────┘  │
│                               │
│  ┌──────────┐ ┌───────────┐  │ Fee & Status
│  │Entry Fee │ │  Status   │  │
│  │  ₹500    │ │ UPCOMING  │  │
│  └──────────┘ └───────────┘  │
│                               │
│  ┌─────────────────────────┐  │ Progress
│  │ Registration: 50/100    │  │
│  │ ▰▰▰▰▰▰▱▱▱▱ 50%         │  │
│  └─────────────────────────┘  │
└───────────────────────────────┘
24dp corners, 8dp elevation
```

### Profile Header

```
┌─────────────────────────────┐
│ ▓▓▓▓ Gradient Header ▓▓▓▓▓▓ │
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│                             │
│        ┌───────┐            │ Avatar (110dp card)
│        │  👤   │            │
│        └───────┘            │
│                             │
│       John Doe              │ Name (28sp)
│       @johndoe              │ Username (16sp)
│                             │
└─────────────────────────────┘
 ┌───────────────────────────┐  Card overlapping
 │ Account Information       │  (24dp corners)
 │                           │  (-60dp top margin)
 │ ┌─────────────────────┐   │
 │ │ ✉️ Email            │   │
 │ │ john@example.com    │   │
 │ └─────────────────────┘   │
 │                           │
 │ ┌─────────────────────┐   │
 │ │ 👤 Role      [Active]│  │
 │ │ User                │  │
 │ └─────────────────────┘   │
 └───────────────────────────┘
```

---

## Animation Guidelines

### Touch Feedback

- Ripple: #30A855F7 (30% opacity purple)
- Duration: 300ms
- Easing: Standard Material curve

### Transitions

- Screen transitions: 250ms slide
- Card elevation change: 150ms
- Color transitions: 200ms

### Loading States

- Progress indicator: White on gradient
- Shimmer effect: gradient_shimmer.xml
- Skeleton screens: Light gray (#F3F4F6)

---

## Accessibility

### Contrast Ratios

- Text on white: 4.5:1 minimum
- Text on gradient: High contrast white/shadow
- Interactive elements: 3:1 minimum

### Touch Targets

- Minimum: 48dp x 48dp
- Buttons: 60dp height
- Icons: 24dp with padding

### Font Sizes

- Minimum: 12sp (captions)
- Body text: 15-16sp
- Headings: 22sp+

---

## Best Practices

### ✅ Do

- Use gradient backgrounds for splash/login/register
- Apply rounded corners (16-24dp)
- Add elevation to important cards (8-16dp)
- Use white text on gradients
- Maintain consistent spacing (16dp, 24dp)
- Use icons in input fields
- Apply gradient to primary buttons

### ❌ Don't

- Mix flat colors with gradients inconsistently
- Use corners less than 12dp
- Overcrowd cards (minimum 16dp padding)
- Use low contrast text
- Ignore elevation hierarchy
- Skip touch feedback
- Use multiple competing gradients

---

## Quick Reference

### Common Patterns

**Card with Gradient Header:**

```xml
<MaterialCardView
    cardCornerRadius="24dp"
    cardElevation="8dp"
    strokeWidth="1dp"
    strokeColor="#10A855F7">

    <LinearLayout orientation="vertical">
        <RelativeLayout background="@drawable/gradient_card">
            <!-- Header content -->
        </RelativeLayout>
        <LinearLayout padding="20dp">
            <!-- Card content -->
        </LinearLayout>
    </LinearLayout>
</MaterialCardView>
```

**Gradient Button:**

```xml
<MaterialButton
    height="60dp"
    backgroundTint="@null"
    background="@drawable/button_gradient"
    cornerRadius="16dp"
    elevation="8dp"
    textColor="@color/white"
    textSize="18sp"
    textStyle="bold" />
```

**Modern Input:**

```xml
<TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    boxCornerRadiusTopStart="16dp"
    boxCornerRadiusTopEnd="16dp"
    boxCornerRadiusBottomStart="16dp"
    boxCornerRadiusBottomEnd="16dp"
    boxStrokeColor="@color/light_primary"
    hintTextColor="@color/light_primary"
    startIconDrawable="@drawable/ic_icon"
    startIconTint="@color/light_primary">

    <TextInputEditText
        textSize="16sp" />
</TextInputLayout>
```

---

**Design System Version**: 2.0  
**Last Updated**: November 19, 2025  
**Maintained by**: ClubCI Design Team
