package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalServiceAreaSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        val view = toGlobalServiceAreaSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalServiceAreaSnapshotViews(mutableListOf<GlobalServiceAreaSnapshot>(item, item)).size, "views")
    }
}
