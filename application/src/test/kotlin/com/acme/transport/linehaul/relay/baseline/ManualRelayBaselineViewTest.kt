package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRelayBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRelayBaseline("code-385", "name-614")
        val view = toManualRelayBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRelayBaselineViews(mutableListOf<ManualRelayBaseline>(item, item)).size, "views")
    }
}
