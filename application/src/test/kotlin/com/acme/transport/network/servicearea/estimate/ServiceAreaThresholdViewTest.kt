package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaThreshold("code-823", "name-768")
        val view = toServiceAreaThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaThresholdViews(mutableListOf<ServiceAreaThreshold>(item, item)).size, "views")
    }
}
