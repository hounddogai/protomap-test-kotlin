package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryFuelSurchargeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryFuelSurchargeAssignment("code-823", "label-122")
        val view = toPrimaryFuelSurchargeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryFuelSurchargeAssignmentViews(mutableListOf<PrimaryFuelSurchargeAssignment>(item, item)).size, "views")
    }
}
