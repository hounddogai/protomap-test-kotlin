package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTrailerYardRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTrailerYardRollup("code-220", "label-545")
        val view = toCappedTrailerYardRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTrailerYardRollupViews(mutableListOf<CappedTrailerYardRollup>(item, item)).size, "views")
    }
}
