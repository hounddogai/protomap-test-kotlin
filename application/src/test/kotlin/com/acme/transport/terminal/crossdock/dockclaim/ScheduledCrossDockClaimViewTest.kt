package com.acme.transport.terminal.crossdock.dockclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCrossDockClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCrossDockClaim("code-721", "name-732")
        val view = toScheduledCrossDockClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCrossDockClaimViews(mutableListOf<ScheduledCrossDockClaim>(item, item)).size, "views")
    }
}
