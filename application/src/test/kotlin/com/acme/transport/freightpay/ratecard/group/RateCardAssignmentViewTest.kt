package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardAssignment("code-583", "title-750")
        val view = toRateCardAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardAssignmentViews(mutableListOf<RateCardAssignment>(item, item)).size, "views")
    }
}
