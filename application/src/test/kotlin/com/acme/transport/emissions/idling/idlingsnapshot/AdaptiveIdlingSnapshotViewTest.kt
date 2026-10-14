package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveIdlingSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveIdlingSnapshot("code-95", "title-970")
        val view = toAdaptiveIdlingSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveIdlingSnapshotViews(mutableListOf<AdaptiveIdlingSnapshot>(item, item)).size, "views")
    }
}
