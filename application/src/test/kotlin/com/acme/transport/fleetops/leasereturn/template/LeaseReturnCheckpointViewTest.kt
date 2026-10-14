package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        val view = toLeaseReturnCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnCheckpointViews(mutableListOf<LeaseReturnCheckpoint>(item, item)).size, "views")
    }
}
