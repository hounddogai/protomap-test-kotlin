package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeGroup("code-507", "title-132")
        val view = toEvChargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeGroupViews(mutableListOf<EvChargeGroup>(item, item)).size, "views")
    }
}
