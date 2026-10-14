package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardAllocation("code-40", "label-721")
        val view = toTrailerYardAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardAllocationViews(mutableListOf<TrailerYardAllocation>(item, item)).size, "views")
    }
}
