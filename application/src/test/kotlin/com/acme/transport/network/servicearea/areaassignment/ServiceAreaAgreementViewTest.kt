package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaAgreement("code-746", "title-774")
        val view = toServiceAreaAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaAgreementViews(mutableListOf<ServiceAreaAgreement>(item, item)).size, "views")
    }
}
