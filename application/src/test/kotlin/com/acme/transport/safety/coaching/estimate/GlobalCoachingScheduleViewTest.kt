package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCoachingScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        val view = toGlobalCoachingScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCoachingScheduleViews(mutableListOf<GlobalCoachingSchedule>(item, item)).size, "views")
    }
}
