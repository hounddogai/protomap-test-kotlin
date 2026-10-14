package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceCheckpoint("code-451", "name-638")
        val view = toClearanceCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceCheckpointViews(mutableListOf<ClearanceCheckpoint>(item, item)).size, "views")
    }
}
