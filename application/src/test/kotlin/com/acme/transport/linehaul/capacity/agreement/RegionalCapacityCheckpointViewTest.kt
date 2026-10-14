package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityCheckpoint("code-9", "name-927")
        val view = toRegionalCapacityCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityCheckpointViews(mutableListOf<RegionalCapacityCheckpoint>(item, item)).size, "views")
    }
}
