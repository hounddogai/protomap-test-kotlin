package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialPolicy("code-563", "label-647")
        val view = toAccessorialPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialPolicyViews(mutableListOf<AccessorialPolicy>(item, item)).size, "views")
    }
}
