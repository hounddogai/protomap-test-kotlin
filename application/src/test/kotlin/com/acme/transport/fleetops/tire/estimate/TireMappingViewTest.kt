package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class TireMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireMapping("code-197", "name-223")
        val view = toTireMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireMappingViews(mutableListOf<TireMapping>(item, item)).size, "views")
    }
}
