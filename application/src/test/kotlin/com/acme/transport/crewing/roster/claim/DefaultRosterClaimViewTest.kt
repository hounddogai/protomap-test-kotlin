package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRosterClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRosterClaim("code-433", "label-830")
        val view = toDefaultRosterClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRosterClaimViews(mutableListOf<DefaultRosterClaim>(item, item)).size, "views")
    }
}
