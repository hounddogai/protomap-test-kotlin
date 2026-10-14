package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEvChargeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEvChargeClaim("code-101", "name-177")
        val view = toStrictEvChargeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEvChargeClaimViews(mutableListOf<StrictEvChargeClaim>(item, item)).size, "views")
    }
}
