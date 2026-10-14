package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualClearanceCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        val view = toManualClearanceCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualClearanceCheckpointViews(mutableListOf<ManualClearanceCheckpoint>(item, item)).size, "views")
    }
}
