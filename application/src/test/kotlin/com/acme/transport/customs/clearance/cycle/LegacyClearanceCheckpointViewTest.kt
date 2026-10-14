package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyClearanceCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyClearanceCheckpoint("code-47", "name-356")
        val view = toLegacyClearanceCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyClearanceCheckpointViews(mutableListOf<LegacyClearanceCheckpoint>(item, item)).size, "views")
    }
}
