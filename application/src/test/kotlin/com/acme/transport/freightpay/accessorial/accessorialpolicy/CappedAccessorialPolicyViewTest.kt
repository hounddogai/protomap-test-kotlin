package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAccessorialPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAccessorialPolicy("code-77", "name-421")
        val view = toCappedAccessorialPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAccessorialPolicyViews(mutableListOf<CappedAccessorialPolicy>(item, item)).size, "views")
    }
}
