package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTariffRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTariffRequest("code-478", "name-645")
        val view = toLenientTariffRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTariffRequestViews(mutableListOf<LenientTariffRequest>(item, item)).size, "views")
    }
}
