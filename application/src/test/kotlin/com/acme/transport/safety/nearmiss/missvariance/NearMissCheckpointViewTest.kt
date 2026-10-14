package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissCheckpoint("code-857", "label-326")
        val view = toNearMissCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissCheckpointViews(mutableListOf<NearMissCheckpoint>(item, item)).size, "views")
    }
}
