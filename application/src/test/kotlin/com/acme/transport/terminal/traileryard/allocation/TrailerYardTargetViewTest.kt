package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardTarget("code-665", "name-342")
        val view = toTrailerYardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardTargetViews(mutableListOf<TrailerYardTarget>(item, item)).size, "views")
    }
}
