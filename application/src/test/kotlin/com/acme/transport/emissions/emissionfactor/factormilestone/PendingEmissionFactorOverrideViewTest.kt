package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingEmissionFactorOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingEmissionFactorOverride("code-970", "name-531")
        val view = toPendingEmissionFactorOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingEmissionFactorOverrideViews(mutableListOf<PendingEmissionFactorOverride>(item, item)).size, "views")
    }
}
