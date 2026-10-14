package com.acme.transport.crewing.roster.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRosterTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRosterTier("code-1", "name-379")
        val view = toPendingRosterTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRosterTierViews(mutableListOf<PendingRosterTier>(item, item)).size, "views")
    }
}
