package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryLeaseReturnScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        val view = toPrimaryLeaseReturnScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryLeaseReturnScheduleViews(mutableListOf<PrimaryLeaseReturnSchedule>(item, item)).size, "views")
    }
}
