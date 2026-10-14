package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerRollup("code-286", "name-523")
        val view = toTrailerRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerRollupViews(mutableListOf<TrailerRollup>(item, item)).size, "views")
    }
}
