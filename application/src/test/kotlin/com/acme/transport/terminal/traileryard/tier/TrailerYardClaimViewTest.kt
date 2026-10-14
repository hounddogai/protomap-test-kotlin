package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardClaim("code-748", "name-33")
        val view = toTrailerYardClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardClaimViews(mutableListOf<TrailerYardClaim>(item, item)).size, "views")
    }
}
