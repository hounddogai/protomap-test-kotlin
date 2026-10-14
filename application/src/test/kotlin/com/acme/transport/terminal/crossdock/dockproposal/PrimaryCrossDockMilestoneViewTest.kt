package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCrossDockMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCrossDockMilestone("code-762", "name-317")
        val view = toPrimaryCrossDockMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCrossDockMilestoneViews(mutableListOf<PrimaryCrossDockMilestone>(item, item)).size, "views")
    }
}
