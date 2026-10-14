package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRosterProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRosterProfile("code-881", "label-971")
        val view = toScheduledRosterProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRosterProfileViews(mutableListOf<ScheduledRosterProfile>(item, item)).size, "views")
    }
}
