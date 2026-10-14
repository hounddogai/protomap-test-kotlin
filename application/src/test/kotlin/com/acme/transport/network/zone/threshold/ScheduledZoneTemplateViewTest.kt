package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledZoneTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledZoneTemplate("code-878", "label-44")
        val view = toScheduledZoneTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledZoneTemplateViews(mutableListOf<ScheduledZoneTemplate>(item, item)).size, "views")
    }
}
