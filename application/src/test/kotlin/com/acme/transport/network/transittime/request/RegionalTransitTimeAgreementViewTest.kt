package com.acme.transport.network.transittime.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTransitTimeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTransitTimeAgreement("code-508", "name-752")
        val view = toRegionalTransitTimeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTransitTimeAgreementViews(mutableListOf<RegionalTransitTimeAgreement>(item, item)).size, "views")
    }
}
