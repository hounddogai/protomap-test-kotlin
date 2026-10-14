package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityAgreement("code-810", "title-81")
        val view = toBatchedCapacityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityAgreementViews(mutableListOf<BatchedCapacityAgreement>(item, item)).size, "views")
    }
}
