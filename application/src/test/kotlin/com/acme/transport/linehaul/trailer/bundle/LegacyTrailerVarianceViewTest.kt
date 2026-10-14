package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTrailerVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTrailerVariance("code-156", "label-790")
        val view = toLegacyTrailerVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTrailerVarianceViews(mutableListOf<LegacyTrailerVariance>(item, item)).size, "views")
    }
}
