package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledNearMissOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledNearMissOverride("code-874", "name-302")
        val view = toScheduledNearMissOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledNearMissOverrideViews(mutableListOf<ScheduledNearMissOverride>(item, item)).size, "views")
    }
}
