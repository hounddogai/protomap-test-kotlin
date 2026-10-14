package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalParcelBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalParcelBatch("code-363", "name-235")
        val view = toGlobalParcelBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalParcelBatchViews(mutableListOf<GlobalParcelBatch>(item, item)).size, "views")
    }
}
