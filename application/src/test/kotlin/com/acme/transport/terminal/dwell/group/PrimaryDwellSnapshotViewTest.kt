package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDwellSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDwellSnapshot("code-518", "label-79")
        val view = toPrimaryDwellSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDwellSnapshotViews(mutableListOf<PrimaryDwellSnapshot>(item, item)).size, "views")
    }
}
