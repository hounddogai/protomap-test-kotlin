package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingHoldAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingHoldAllocation("code-420", "title-896")
        val view = toRollingHoldAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingHoldAllocationViews(mutableListOf<RollingHoldAllocation>(item, item)).size, "views")
    }
}
