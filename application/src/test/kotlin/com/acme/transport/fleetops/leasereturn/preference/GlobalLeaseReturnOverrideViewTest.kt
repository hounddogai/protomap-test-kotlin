package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalLeaseReturnOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalLeaseReturnOverride("code-48", "label-713")
        val view = toGlobalLeaseReturnOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalLeaseReturnOverrideViews(mutableListOf<GlobalLeaseReturnOverride>(item, item)).size, "views")
    }
}
