package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerVariance("code-39", "name-175")
        val view = toManualTrailerVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerVarianceViews(mutableListOf<ManualTrailerVariance>(item, item)).size, "views")
    }
}
