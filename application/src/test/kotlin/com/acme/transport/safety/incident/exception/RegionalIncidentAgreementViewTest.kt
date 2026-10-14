package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalIncidentAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalIncidentAgreement("code-640", "title-894")
        val view = toRegionalIncidentAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalIncidentAgreementViews(mutableListOf<RegionalIncidentAgreement>(item, item)).size, "views")
    }
}
