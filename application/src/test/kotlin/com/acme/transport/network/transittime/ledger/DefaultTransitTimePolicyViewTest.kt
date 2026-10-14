package com.acme.transport.network.transittime.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        val view = toDefaultTransitTimePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimePolicyViews(mutableListOf<DefaultTransitTimePolicy>(item, item)).size, "views")
    }
}
