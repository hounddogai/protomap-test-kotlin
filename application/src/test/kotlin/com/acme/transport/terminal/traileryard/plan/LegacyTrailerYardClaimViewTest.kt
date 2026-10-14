package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTrailerYardClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        val view = toLegacyTrailerYardClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTrailerYardClaimViews(mutableListOf<LegacyTrailerYardClaim>(item, item)).size, "views")
    }
}
