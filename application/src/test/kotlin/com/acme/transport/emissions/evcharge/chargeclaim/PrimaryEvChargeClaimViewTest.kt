package com.acme.transport.emissions.evcharge.chargeclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryEvChargeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryEvChargeClaim("code-909", "label-644")
        val view = toPrimaryEvChargeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryEvChargeClaimViews(mutableListOf<PrimaryEvChargeClaim>(item, item)).size, "views")
    }
}
