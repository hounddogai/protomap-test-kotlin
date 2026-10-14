package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterClaim("code-494", "name-31")
        val view = toRosterClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterClaimViews(mutableListOf<RosterClaim>(item, item)).size, "views")
    }
}
