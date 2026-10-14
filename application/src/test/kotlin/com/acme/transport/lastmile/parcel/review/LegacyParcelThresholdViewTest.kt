package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyParcelThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyParcelThreshold("code-91", "label-153")
        val view = toLegacyParcelThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyParcelThresholdViews(mutableListOf<LegacyParcelThreshold>(item, item)).size, "views")
    }
}
