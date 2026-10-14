package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingEmissionFactorQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        val view = toRollingEmissionFactorQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingEmissionFactorQuotaViews(mutableListOf<RollingEmissionFactorQuota>(item, item)).size, "views")
    }
}
