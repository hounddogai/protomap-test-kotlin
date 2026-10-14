package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulTarget("code-563", "label-120")
        val view = toBackhaulTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulTargetViews(mutableListOf<BackhaulTarget>(item, item)).size, "views")
    }
}
