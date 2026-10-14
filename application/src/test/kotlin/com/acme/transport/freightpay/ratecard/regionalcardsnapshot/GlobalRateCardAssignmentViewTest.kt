package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRateCardAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRateCardAssignment("code-92", "name-42")
        val view = toGlobalRateCardAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRateCardAssignmentViews(mutableListOf<GlobalRateCardAssignment>(item, item)).size, "views")
    }
}
