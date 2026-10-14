package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelaySchedule("code-171", "label-498")
        val view = toRelayScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayScheduleViews(mutableListOf<RelaySchedule>(item, item)).size, "views")
    }
}
