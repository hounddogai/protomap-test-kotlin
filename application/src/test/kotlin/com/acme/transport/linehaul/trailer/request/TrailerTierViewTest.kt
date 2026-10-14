package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerTier("code-537", "label-806")
        val view = toTrailerTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerTierViews(mutableListOf<TrailerTier>(item, item)).size, "views")
    }
}
