package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedServiceAreaAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedServiceAreaAgreement("code-450", "name-321")
        val view = toWeightedServiceAreaAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedServiceAreaAgreementViews(mutableListOf<WeightedServiceAreaAgreement>(item, item)).size, "views")
    }
}
