package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceSnapshot("code-432", "title-318")
        val view = toClearanceSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceSnapshotViews(mutableListOf<ClearanceSnapshot>(item, item)).size, "views")
    }
}
