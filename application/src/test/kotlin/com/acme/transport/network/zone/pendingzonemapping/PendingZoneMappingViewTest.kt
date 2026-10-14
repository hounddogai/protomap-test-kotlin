package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingZoneMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingZoneMapping("code-271", "label-999")
        val view = toPendingZoneMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingZoneMappingViews(mutableListOf<PendingZoneMapping>(item, item)).size, "views")
    }
}
