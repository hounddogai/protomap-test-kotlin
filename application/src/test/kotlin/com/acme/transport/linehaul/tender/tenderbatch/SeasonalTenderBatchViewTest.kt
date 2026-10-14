package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTenderBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTenderBatch("code-101", "title-655")
        val view = toSeasonalTenderBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTenderBatchViews(mutableListOf<SeasonalTenderBatch>(item, item)).size, "views")
    }
}
