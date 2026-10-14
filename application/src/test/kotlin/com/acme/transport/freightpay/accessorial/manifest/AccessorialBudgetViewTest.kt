package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialBudget("code-809", "title-633")
        val view = toAccessorialBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialBudgetViews(mutableListOf<AccessorialBudget>(item, item)).size, "views")
    }
}
