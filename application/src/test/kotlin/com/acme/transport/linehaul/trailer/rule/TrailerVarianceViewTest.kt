package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerVariance("code-389", "title-59")
        val view = toTrailerVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerVarianceViews(mutableListOf<TrailerVariance>(item, item)).size, "views")
    }
}
