package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTariffClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTariffClaim("code-661", "label-703")
        val view = toPendingTariffClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTariffClaimViews(mutableListOf<PendingTariffClaim>(item, item)).size, "views")
    }
}
