package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTariffPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTariffPolicy("code-982", "label-409")
        val view = toLenientTariffPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTariffPolicyViews(mutableListOf<LenientTariffPolicy>(item, item)).size, "views")
    }
}
