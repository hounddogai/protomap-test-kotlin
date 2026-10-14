package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTenderCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTenderCycle("code-409", "title-439")
        val view = toScheduledTenderCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTenderCycleViews(mutableListOf<ScheduledTenderCycle>(item, item)).size, "views")
    }
}
