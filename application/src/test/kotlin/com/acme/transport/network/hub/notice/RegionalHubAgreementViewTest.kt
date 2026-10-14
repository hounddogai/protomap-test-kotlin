package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalHubAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalHubAgreement("code-141", "name-246")
        val view = toRegionalHubAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalHubAgreementViews(mutableListOf<RegionalHubAgreement>(item, item)).size, "views")
    }
}
