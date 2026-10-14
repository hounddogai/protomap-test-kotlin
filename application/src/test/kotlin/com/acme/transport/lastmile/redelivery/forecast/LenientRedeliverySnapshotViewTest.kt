package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRedeliverySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        val view = toLenientRedeliverySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRedeliverySnapshotViews(mutableListOf<LenientRedeliverySnapshot>(item, item)).size, "views")
    }
}
