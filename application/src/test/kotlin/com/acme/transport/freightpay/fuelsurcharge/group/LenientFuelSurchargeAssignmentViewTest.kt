package com.acme.transport.freightpay.fuelsurcharge.group

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFuelSurchargeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFuelSurchargeAssignment("code-41", "label-39")
        val view = toLenientFuelSurchargeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFuelSurchargeAssignmentViews(mutableListOf<LenientFuelSurchargeAssignment>(item, item)).size, "views")
    }
}
