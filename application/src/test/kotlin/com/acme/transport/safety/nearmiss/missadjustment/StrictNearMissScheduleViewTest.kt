package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictNearMissScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictNearMissSchedule("code-545", "name-797")
        val view = toStrictNearMissScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictNearMissScheduleViews(mutableListOf<StrictNearMissSchedule>(item, item)).size, "views")
    }
}
