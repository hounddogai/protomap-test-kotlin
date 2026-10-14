package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCutoffScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCutoffSchedule("code-773", "label-511")
        val view = toDefaultCutoffScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCutoffScheduleViews(mutableListOf<DefaultCutoffSchedule>(item, item)).size, "views")
    }
}
