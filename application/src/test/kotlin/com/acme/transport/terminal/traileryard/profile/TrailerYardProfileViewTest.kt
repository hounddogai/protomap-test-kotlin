package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardProfile("code-410", "label-572")
        val view = toTrailerYardProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardProfileViews(mutableListOf<TrailerYardProfile>(item, item)).size, "views")
    }
}
