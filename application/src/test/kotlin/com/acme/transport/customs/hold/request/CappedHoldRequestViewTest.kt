package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedHoldRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedHoldRequest("code-264", "title-970")
        val view = toCappedHoldRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedHoldRequestViews(mutableListOf<CappedHoldRequest>(item, item)).size, "views")
    }
}
