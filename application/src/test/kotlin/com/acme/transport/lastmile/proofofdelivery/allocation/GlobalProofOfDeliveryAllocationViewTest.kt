package com.acme.transport.lastmile.proofofdelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalProofOfDeliveryAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        val view = toGlobalProofOfDeliveryAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalProofOfDeliveryAllocationViews(mutableListOf<GlobalProofOfDeliveryAllocation>(item, item)).size, "views")
    }
}
