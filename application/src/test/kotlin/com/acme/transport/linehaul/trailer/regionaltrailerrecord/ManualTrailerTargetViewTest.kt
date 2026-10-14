package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerTarget("code-621", "name-522")
        val view = toManualTrailerTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerTargetViews(mutableListOf<ManualTrailerTarget>(item, item)).size, "views")
    }
}
