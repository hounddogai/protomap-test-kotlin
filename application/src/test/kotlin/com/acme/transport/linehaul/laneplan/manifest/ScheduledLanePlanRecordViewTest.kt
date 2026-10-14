package com.acme.transport.linehaul.laneplan.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLanePlanRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        val view = toScheduledLanePlanRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLanePlanRecordViews(mutableListOf<ScheduledLanePlanRecord>(item, item)).size, "views")
    }
}
