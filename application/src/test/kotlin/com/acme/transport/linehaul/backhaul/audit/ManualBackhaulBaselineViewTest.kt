package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualBackhaulBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualBackhaulBaseline("code-509", "label-586")
        val view = toManualBackhaulBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualBackhaulBaselineViews(mutableListOf<ManualBackhaulBaseline>(item, item)).size, "views")
    }
}
