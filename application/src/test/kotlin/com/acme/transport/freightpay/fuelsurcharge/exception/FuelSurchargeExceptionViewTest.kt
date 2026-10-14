package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeException("code-559", "title-912")
        val view = toFuelSurchargeExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeExceptionViews(mutableListOf<FuelSurchargeException>(item, item)).size, "views")
    }
}
