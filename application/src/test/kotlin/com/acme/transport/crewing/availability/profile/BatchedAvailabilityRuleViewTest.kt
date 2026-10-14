package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAvailabilityRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAvailabilityRule("code-387", "name-902")
        val view = toBatchedAvailabilityRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAvailabilityRuleViews(mutableListOf<BatchedAvailabilityRule>(item, item)).size, "views")
    }
}
