package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityAgreement("code-709", "label-133")
        val view = toAvailabilityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityAgreementViews(mutableListOf<AvailabilityAgreement>(item, item)).size, "views")
    }
}
