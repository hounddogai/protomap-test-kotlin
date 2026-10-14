package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCutoffVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCutoffVariance("code-532", "title-127")
        val view = toAdaptiveCutoffVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCutoffVarianceViews(mutableListOf<AdaptiveCutoffVariance>(item, item)).size, "views")
    }
}
