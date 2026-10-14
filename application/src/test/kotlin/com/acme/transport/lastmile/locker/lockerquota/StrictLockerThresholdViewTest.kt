package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictLockerThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictLockerThreshold("code-178", "name-823")
        val view = toStrictLockerThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictLockerThresholdViews(mutableListOf<StrictLockerThreshold>(item, item)).size, "views")
    }
}
