package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissAllocation("code-428", "name-155")
        val view = toNearMissAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissAllocationViews(mutableListOf<NearMissAllocation>(item, item)).size, "views")
    }
}
