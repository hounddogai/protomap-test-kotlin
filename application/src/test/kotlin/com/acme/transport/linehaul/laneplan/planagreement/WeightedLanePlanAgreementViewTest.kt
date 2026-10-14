package com.acme.transport.linehaul.laneplan.planagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedLanePlanAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedLanePlanAgreement("code-318", "label-806")
        val view = toWeightedLanePlanAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedLanePlanAgreementViews(mutableListOf<WeightedLanePlanAgreement>(item, item)).size, "views")
    }
}
