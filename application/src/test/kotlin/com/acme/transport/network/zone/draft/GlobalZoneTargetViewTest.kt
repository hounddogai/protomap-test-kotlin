package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalZoneTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalZoneTarget("code-917", "title-215")
        val view = toGlobalZoneTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalZoneTargetViews(mutableListOf<GlobalZoneTarget>(item, item)).size, "views")
    }
}
