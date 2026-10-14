package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLockerReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLockerReview("code-729", "name-104")
        val view = toScheduledLockerReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLockerReviewViews(mutableListOf<ScheduledLockerReview>(item, item)).size, "views")
    }
}
