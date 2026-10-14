package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTariffVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTariffVariance("code-85", "name-308")
        val view = toManualTariffVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTariffVarianceViews(mutableListOf<ManualTariffVariance>(item, item)).size, "views")
    }
}
