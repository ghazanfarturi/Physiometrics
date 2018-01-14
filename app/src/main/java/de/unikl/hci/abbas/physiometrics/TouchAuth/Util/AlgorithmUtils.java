package de.unikl.hci.abbas.physiometrics.TouchAuth.Util;

/**
 * Created by abbas on 12/31/17.
 */

public class AlgorithmUtils {
    public static void quickSort(double[] nums, int l, int r) {
        if (l >= r) return;
        int i = l, j = r;
        double tmp = nums[l];
        while (i < j) {
            while (i < j && nums[j] > tmp) --j;
            if (i < j) {
                nums[i++] = nums[j];
            }
            while (i < j && nums[i] < tmp) ++i;
            if (i < j) {
                nums[j--] = nums[i];
            }
        }
        nums[i] = tmp;
        quickSort(nums, l, i - 1);
        quickSort(nums, i + 1, r);
    }
}
