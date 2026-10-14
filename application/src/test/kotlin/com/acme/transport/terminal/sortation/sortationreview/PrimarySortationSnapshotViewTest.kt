package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimarySortationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimarySortationSnapshot("code-285", "label-767")
        val view = toPrimarySortationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimarySortationSnapshotViews(mutableListOf<PrimarySortationSnapshot>(item, item)).size, "views")
    }
}
