package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLockerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLockerProfile("code-866", "name-380")
        val view = toRegionalLockerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLockerProfileViews(mutableListOf<RegionalLockerProfile>(item, item)).size, "views")
    }
}
