package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTrailerBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTrailerBatch("code-782", "title-286")
        val view = toRegionalTrailerBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTrailerBatchViews(mutableListOf<RegionalTrailerBatch>(item, item)).size, "views")
    }
}
