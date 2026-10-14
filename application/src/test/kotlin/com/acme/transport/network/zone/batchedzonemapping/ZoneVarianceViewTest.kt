package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneVariance("code-359", "label-497")
        val view = toZoneVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneVarianceViews(mutableListOf<ZoneVariance>(item, item)).size, "views")
    }
}
