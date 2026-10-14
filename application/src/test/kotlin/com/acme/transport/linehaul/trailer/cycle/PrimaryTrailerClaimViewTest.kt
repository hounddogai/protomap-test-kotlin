package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTrailerClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTrailerClaim("code-961", "label-888")
        val view = toPrimaryTrailerClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTrailerClaimViews(mutableListOf<PrimaryTrailerClaim>(item, item)).size, "views")
    }
}
