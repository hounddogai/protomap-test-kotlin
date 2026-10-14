package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffSchedule("code-605", "title-565")
        val view = toCutoffScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffScheduleViews(mutableListOf<CutoffSchedule>(item, item)).size, "views")
    }
}
