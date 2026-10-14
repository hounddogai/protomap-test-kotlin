package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardQuota("code-567", "label-364")
        val view = toTrailerYardQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardQuotaViews(mutableListOf<TrailerYardQuota>(item, item)).size, "views")
    }
}
