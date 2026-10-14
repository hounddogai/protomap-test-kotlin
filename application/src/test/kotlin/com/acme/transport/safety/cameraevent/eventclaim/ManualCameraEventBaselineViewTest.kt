package com.acme.transport.safety.cameraevent.eventclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventBaseline("code-111", "name-458")
        val view = toManualCameraEventBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventBaselineViews(mutableListOf<ManualCameraEventBaseline>(item, item)).size, "views")
    }
}
