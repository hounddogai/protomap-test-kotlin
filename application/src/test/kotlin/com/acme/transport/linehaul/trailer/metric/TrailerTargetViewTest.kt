package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerTarget("code-349", "label-502")
        val view = toTrailerTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerTargetViews(mutableListOf<TrailerTarget>(item, item)).size, "views")
    }
}
