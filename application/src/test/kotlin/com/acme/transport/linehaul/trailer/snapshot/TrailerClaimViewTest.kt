package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerClaim("code-822", "label-506")
        val view = toTrailerClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerClaimViews(mutableListOf<TrailerClaim>(item, item)).size, "views")
    }
}
