package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredEvChargeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredEvChargeDigest("code-193", "name-549")
        val view = toTieredEvChargeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredEvChargeDigestViews(mutableListOf<TieredEvChargeDigest>(item, item)).size, "views")
    }
}
