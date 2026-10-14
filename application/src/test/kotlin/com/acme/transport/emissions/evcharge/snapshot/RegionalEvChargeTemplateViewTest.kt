package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalEvChargeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalEvChargeTemplate("code-690", "label-660")
        val view = toRegionalEvChargeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalEvChargeTemplateViews(mutableListOf<RegionalEvChargeTemplate>(item, item)).size, "views")
    }
}
