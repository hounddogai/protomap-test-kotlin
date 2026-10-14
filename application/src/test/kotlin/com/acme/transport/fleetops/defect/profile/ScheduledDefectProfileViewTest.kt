package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDefectProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDefectProfile("code-128", "name-384")
        val view = toScheduledDefectProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDefectProfileViews(mutableListOf<ScheduledDefectProfile>(item, item)).size, "views")
    }
}
