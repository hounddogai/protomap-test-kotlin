package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityAgreement("code-940", "title-529")
        val view = toCapacityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityAgreementViews(mutableListOf<CapacityAgreement>(item, item)).size, "views")
    }
}
