package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveZoneAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        val view = toAdaptiveZoneAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveZoneAgreementViews(mutableListOf<AdaptiveZoneAgreement>(item, item)).size, "views")
    }
}
