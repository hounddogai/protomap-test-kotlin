package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class HubAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubAgreement("code-816", "label-266")
        val view = toHubAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubAgreementViews(mutableListOf<HubAgreement>(item, item)).size, "views")
    }
}
