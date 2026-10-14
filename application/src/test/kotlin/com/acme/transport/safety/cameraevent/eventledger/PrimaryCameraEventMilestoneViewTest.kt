package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCameraEventMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCameraEventMilestone("code-879", "title-710")
        val view = toPrimaryCameraEventMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCameraEventMilestoneViews(mutableListOf<PrimaryCameraEventMilestone>(item, item)).size, "views")
    }
}
