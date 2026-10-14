package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCrossDockSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCrossDockSnapshot("code-367", "name-91")
        val view = toDefaultCrossDockSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCrossDockSnapshotViews(mutableListOf<DefaultCrossDockSnapshot>(item, item)).size, "views")
    }
}
