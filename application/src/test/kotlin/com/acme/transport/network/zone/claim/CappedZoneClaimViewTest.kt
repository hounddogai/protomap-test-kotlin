package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedZoneClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedZoneClaim("code-767", "name-127")
        val view = toCappedZoneClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedZoneClaimViews(mutableListOf<CappedZoneClaim>(item, item)).size, "views")
    }
}
