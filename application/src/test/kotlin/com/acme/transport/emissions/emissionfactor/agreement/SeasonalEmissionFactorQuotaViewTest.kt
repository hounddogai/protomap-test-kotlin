package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalEmissionFactorQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalEmissionFactorQuota("code-983", "name-350")
        val view = toSeasonalEmissionFactorQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalEmissionFactorQuotaViews(mutableListOf<SeasonalEmissionFactorQuota>(item, item)).size, "views")
    }
}
