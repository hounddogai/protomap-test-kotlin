package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryFuelSurchargeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryFuelSurchargeTier("code-350", "label-641")
        val view = toPrimaryFuelSurchargeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryFuelSurchargeTierViews(mutableListOf<PrimaryFuelSurchargeTier>(item, item)).size, "views")
    }
}
