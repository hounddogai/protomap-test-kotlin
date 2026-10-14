package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalServiceAreaThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalServiceAreaThreshold("code-929", "name-689")
        val view = toRegionalServiceAreaThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalServiceAreaThresholdViews(mutableListOf<RegionalServiceAreaThreshold>(item, item)).size, "views")
    }
}
