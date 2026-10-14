package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingEmissionFactorClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingEmissionFactorClaim("code-957", "label-663")
        val view = toPendingEmissionFactorClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingEmissionFactorClaimViews(mutableListOf<PendingEmissionFactorClaim>(item, item)).size, "views")
    }
}
