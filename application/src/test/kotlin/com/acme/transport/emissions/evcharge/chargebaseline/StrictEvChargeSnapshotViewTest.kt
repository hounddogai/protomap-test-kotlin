package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEvChargeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEvChargeSnapshot("code-850", "name-45")
        val view = toStrictEvChargeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEvChargeSnapshotViews(mutableListOf<StrictEvChargeSnapshot>(item, item)).size, "views")
    }
}
