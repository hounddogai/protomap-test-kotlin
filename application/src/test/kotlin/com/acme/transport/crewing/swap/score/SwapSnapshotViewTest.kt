package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapSnapshot("code-795", "title-470")
        val view = toSwapSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapSnapshotViews(mutableListOf<SwapSnapshot>(item, item)).size, "views")
    }
}
