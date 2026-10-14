package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeSnapshot("code-409", "label-378")
        val view = toEvChargeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeSnapshotViews(mutableListOf<EvChargeSnapshot>(item, item)).size, "views")
    }
}
