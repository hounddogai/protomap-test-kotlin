package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldSnapshot("code-237", "label-901")
        val view = toHoldSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldSnapshotViews(mutableListOf<HoldSnapshot>(item, item)).size, "views")
    }
}
