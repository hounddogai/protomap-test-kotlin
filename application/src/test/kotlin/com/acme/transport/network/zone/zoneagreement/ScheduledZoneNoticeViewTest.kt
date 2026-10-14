package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledZoneNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledZoneNotice("code-442", "name-884")
        val view = toScheduledZoneNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledZoneNoticeViews(mutableListOf<ScheduledZoneNotice>(item, item)).size, "views")
    }
}
