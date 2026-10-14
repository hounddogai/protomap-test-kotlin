package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualBackhaulAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualBackhaulAllocation("code-656", "name-725")
        val view = toManualBackhaulAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualBackhaulAllocationViews(mutableListOf<ManualBackhaulAllocation>(item, item)).size, "views")
    }
}
