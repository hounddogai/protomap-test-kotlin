package com.acme.transport.network.servicearea.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaAllocation("code-181", "name-421")
        val view = toServiceAreaAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaAllocationViews(mutableListOf<ServiceAreaAllocation>(item, item)).size, "views")
    }
}
