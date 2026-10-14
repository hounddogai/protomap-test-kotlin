package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeRequest("code-579", "name-898")
        val view = toEvChargeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeRequestViews(mutableListOf<EvChargeRequest>(item, item)).size, "views")
    }
}
