package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCapacityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        val view = toGlobalCapacityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCapacityAgreementViews(mutableListOf<GlobalCapacityAgreement>(item, item)).size, "views")
    }
}
