package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingBackhaulBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        val view = toRollingBackhaulBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingBackhaulBaselineViews(mutableListOf<RollingBackhaulBaseline>(item, item)).size, "views")
    }
}
