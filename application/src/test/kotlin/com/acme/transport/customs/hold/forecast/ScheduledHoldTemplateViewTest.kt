package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHoldTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHoldTemplate("code-615", "title-962")
        val view = toScheduledHoldTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHoldTemplateViews(mutableListOf<ScheduledHoldTemplate>(item, item)).size, "views")
    }
}
