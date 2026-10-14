package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTrailerYardTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTrailerYardTier("code-237", "title-441")
        val view = toCappedTrailerYardTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTrailerYardTierViews(mutableListOf<CappedTrailerYardTier>(item, item)).size, "views")
    }
}
