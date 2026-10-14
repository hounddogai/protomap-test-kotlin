package com.acme.transport.network.servicearea.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServiceAreaAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServiceAreaAllocation("code-923", "name-161")
        val view = toPendingServiceAreaAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServiceAreaAllocationViews(mutableListOf<PendingServiceAreaAllocation>(item, item)).size, "views")
    }
}
