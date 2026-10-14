package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeMapping("code-566", "name-912")
        val view = toEvChargeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeMappingViews(mutableListOf<EvChargeMapping>(item, item)).size, "views")
    }
}
