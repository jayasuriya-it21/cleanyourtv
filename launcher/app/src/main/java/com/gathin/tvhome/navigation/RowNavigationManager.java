package com.gathin.tvhome.navigation;

/**
 * Pure Java deterministic navigation decision engine for horizontal content shelves
 * and vertical transitions between utility controls, content shelves, and app dock/grid.
 * Completely decoupled from Android Views for thorough unit testing.
 */
public final class RowNavigationManager {

    private RowNavigationManager() {
    }

    /**
     * Resolves horizontal D-pad movement within a content shelf.
     * Enforces strict boundary absorption: focus NEVER escapes row ends horizontally.
     *
     * @param currentPosition Current focused index in shelf.
     * @param itemCount Total number of items in shelf.
     * @param isLeft True if DPAD_LEFT was pressed, false if DPAD_RIGHT.
     * @param isRtl True if layout is right-to-left.
     * @return Target index to focus. If at boundary, returns currentPosition (action swallowed).
     */
    public static int handleHorizontalKey(int currentPosition, int itemCount, boolean isLeft, boolean isRtl) {
        if (itemCount <= 0) {
            return -1;
        }

        boolean moveNext = isRtl ? isLeft : !isLeft;

        if (moveNext) {
            if (currentPosition >= itemCount - 1) {
                // At rightmost/end boundary: absorb event, do not lose focus
                return itemCount - 1;
            }
            return currentPosition + 1;
        } else {
            if (currentPosition <= 0) {
                // At leftmost/start boundary: absorb event, do not lose focus
                return 0;
            }
            return currentPosition - 1;
        }
    }

    /**
     * Checks if moving in the specified horizontal direction would hit an edge boundary.
     */
    public static boolean isHorizontalBoundary(int currentPosition, int itemCount, boolean isLeft, boolean isRtl) {
        if (itemCount <= 0) return true;
        boolean moveNext = isRtl ? isLeft : !isLeft;
        if (moveNext) {
            return currentPosition >= itemCount - 1;
        } else {
            return currentPosition <= 0;
        }
    }

    /**
     * Calculates DOWN target from shelf to dock/grid using horizontal center coordinates.
     * Finds the dock item whose horizontal center is closest to the poster center.
     *
     * @param posterCenterX Center X coordinate of focused poster.
     * @param dockItemCenters Array of center X coordinates for active dock items.
     * @return Target dock index (0-based), or 0 fallback.
     */
    public static int calculateDownTargetGeometric(float posterCenterX, float[] dockItemCenters) {
        if (dockItemCenters == null || dockItemCenters.length == 0) {
            return 0;
        }

        int closestIndex = 0;
        float minDistance = Math.abs(posterCenterX - dockItemCenters[0]);

        for (int i = 1; i < dockItemCenters.length; i++) {
            float distance = Math.abs(posterCenterX - dockItemCenters[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Proportional index fallback for DOWN target when geometric coordinates are unavailable.
     *
     * @param posterIndex Focused poster index.
     * @param posterCount Total posters in shelf.
     * @param dockItemCount Total items in dock row.
     * @param columns Grid columns count (typically 4).
     * @return Deterministic dock index.
     */
    public static int calculateDownTargetProportional(int posterIndex, int posterCount, int dockItemCount, int columns) {
        if (dockItemCount <= 0) return 0;
        int activeCols = Math.min(dockItemCount, Math.max(1, columns));
        if (posterCount <= 1 || activeCols <= 1) return 0;

        double ratio = (double) Math.max(0, Math.min(posterIndex, posterCount - 1)) / (posterCount - 1);
        int targetCol = (int) Math.round(ratio * (activeCols - 1));
        return Math.max(0, Math.min(targetCol, dockItemCount - 1));
    }

    /**
     * Calculates UP target from dock/grid to shelf.
     *
     * @param dockIndex Currently focused dock/grid index.
     * @param columns Total columns per row.
     * @param posterCount Total posters in shelf.
     * @param lastFocusedPosterIndex Last focused poster from FocusMemory.
     * @param originatedFromSpotlight True if focus previously came down from the shelf.
     * @return Target poster index in shelf, or -1 if shelf is empty.
     */
    public static int calculateUpTarget(int dockIndex, int columns, int posterCount,
                                        int lastFocusedPosterIndex, boolean originatedFromSpotlight) {
        if (posterCount <= 0) {
            return -1;
        }

        if (originatedFromSpotlight) {
            return Math.max(0, Math.min(lastFocusedPosterIndex, posterCount - 1));
        }

        // Proportional nearest-column mapping
        int numCols = Math.max(1, columns);
        int col = Math.max(0, dockIndex) % numCols;
        if (numCols <= 1 || posterCount <= 1) {
            return 0;
        }

        double ratio = (double) col / (numCols - 1);
        int targetPoster = (int) Math.round(ratio * (posterCount - 1));
        return Math.max(0, Math.min(targetPoster, posterCount - 1));
    }

    /**
     * Calculates UP target from shelf to top bar utility buttons.
     *
     * @param posterIndex Focused poster index.
     * @param posterCount Total posters in shelf.
     * @param buttonCount Total focusable top buttons.
     * @return Target button index, or -1 if no buttons are focusable.
     */
    /**
     * Pure Java abstraction of a candidate utility control in the top bar.
     * Decoupled from Android View hierarchy for unit testability.
     */
    public static final class UtilityCandidate {
        public final int id;
        public final float centerX;
        public final boolean isValid;

        public UtilityCandidate(int id, float centerX, boolean isValid) {
            this.id = id;
            this.centerX = centerX;
            this.isValid = isValid;
        }

        public static UtilityCandidate valid(int id, float centerX) {
            return new UtilityCandidate(id, centerX, true);
        }

        public static UtilityCandidate invalid(int id) {
            return new UtilityCandidate(id, 0f, false);
        }
    }

    /**
     * Evaluates candidates by distance from poster horizontal center.
     * Only valid candidates (non-null, attached, visible, shown, enabled, focusable, non-zero size) are considered.
     * In case of exact tie in distance, deterministic tie-breaking selects the lower index (leftmost candidate).
     *
     * @param posterCenterX Horizontal center of the focused poster.
     * @param candidates List of candidates.
     * @return Index within candidates list of the chosen valid candidate, or -1 if no candidate is valid.
     */
    public static int selectBestUtilityTarget(float posterCenterX, java.util.List<UtilityCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return -1;
        }

        int bestIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < candidates.size(); i++) {
            UtilityCandidate c = candidates.get(i);
            if (c == null || !c.isValid) {
                continue;
            }
            float dist = Math.abs(posterCenterX - c.centerX);
            // Strictly less ensures that in an exact tie, the lower index (leftmost) is preserved
            if (dist < minDistance) {
                minDistance = dist;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    /**
     * Proportional target selection among valid candidates when geometric coordinates are unmeasured.
     *
     * @param posterIndex Focused poster index.
     * @param posterCount Total items in shelf.
     * @param candidates List of utility candidates.
     * @return Index within candidates list of the chosen valid candidate, or -1 if no candidate is valid.
     */
    public static int selectBestUtilityTargetProportional(int posterIndex, int posterCount, java.util.List<UtilityCandidate> candidates) {
        if (candidates == null || candidates.isEmpty() || posterCount <= 0) {
            return -1;
        }
        java.util.List<Integer> validIndices = new java.util.ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            UtilityCandidate c = candidates.get(i);
            if (c != null && c.isValid) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return -1;
        }
        int validCount = validIndices.size();
        if (validCount == 1 || posterCount <= 1) {
            return validIndices.get(0);
        }
        double ratio = (double) Math.max(0, Math.min(posterIndex, posterCount - 1)) / (posterCount - 1);
        int targetSlot = (int) Math.round(ratio * (validCount - 1));
        targetSlot = Math.max(0, Math.min(targetSlot, validCount - 1));
        return validIndices.get(targetSlot);
    }
}
