package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeAssignment("code-970", "label-988")
        val view = toDisputeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeAssignmentViews(mutableListOf<DisputeAssignment>(item, item)).size, "views")
    }
}
