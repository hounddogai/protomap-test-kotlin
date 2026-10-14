package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeMilestone("code-22", "name-737")
        val view = toEvChargeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeMilestoneViews(mutableListOf<EvChargeMilestone>(item, item)).size, "views")
    }
}
