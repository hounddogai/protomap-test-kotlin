package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerProfile("code-785", "name-627")
        val view = toTrailerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerProfileViews(mutableListOf<TrailerProfile>(item, item)).size, "views")
    }
}
