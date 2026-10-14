package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAvailabilityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultAvailabilityAgreement("code-873", "label-730")
        val view = toDefaultAvailabilityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultAvailabilityAgreementViews(mutableListOf<DefaultAvailabilityAgreement>(item, item)).size, "views")
    }
}
