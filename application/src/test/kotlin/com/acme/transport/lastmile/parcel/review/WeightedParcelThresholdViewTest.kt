package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedParcelThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedParcelThreshold("code-380", "title-750")
        val view = toWeightedParcelThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedParcelThresholdViews(mutableListOf<WeightedParcelThreshold>(item, item)).size, "views")
    }
}
