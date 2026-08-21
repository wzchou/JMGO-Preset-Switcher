# JMGO Preset Switcher

Android TV / Google TV utility for cycling JMGO saved projector positions with one remote-control button.

## v0.1 test goal

1. Install the APK on the JMGO projector.
2. Open **JMGO Preset Switcher**.
3. Enable its Accessibility Service.
4. Choose **Learn Remote Button**, then short-press the desired remote button (start with the ≡ / Menu button).
5. Set the number of existing JMGO saved positions.
6. Press **Test: switch to next preset** or the learned button.

The service consumes the trigger key, remembers the current preset index, opens the most likely JMGO projector settings activity, and navigates visible Accessibility nodes toward **AI Spatial Image Memory** / saved positions.

JMGO does not document a public API for recalling saved positions. The first projector test is intended to identify the exact package/activity and Accessibility node labels on the target N1S firmware. Once those are captured, the navigation can be made fast and deterministic.

### Current capabilities

- Android TV launcher app
- Global remote key capture via AccessibilityService
- Learn any firmware-exposed key code (including trying Prime Video)
- Default trigger: `KEYCODE_MENU`
- Configurable 2–10 saved positions
- Cyclic next-preset state
- Multilingual label matching for JMGO settings
- GitHub Actions debug APK build
