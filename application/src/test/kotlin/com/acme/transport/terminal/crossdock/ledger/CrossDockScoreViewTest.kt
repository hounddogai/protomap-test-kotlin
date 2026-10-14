package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockScore("code-871", "label-449")
        val view = toCrossDockScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockScoreViews(mutableListOf<CrossDockScore>(item, item)).size, "views")
    }
}
