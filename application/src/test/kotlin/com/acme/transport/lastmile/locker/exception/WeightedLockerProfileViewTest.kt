package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedLockerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedLockerProfile("code-891", "title-413")
        val view = toWeightedLockerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedLockerProfileViews(mutableListOf<WeightedLockerProfile>(item, item)).size, "views")
    }
}
