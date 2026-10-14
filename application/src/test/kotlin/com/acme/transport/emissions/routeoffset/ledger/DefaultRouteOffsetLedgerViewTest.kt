package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRouteOffsetLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRouteOffsetLedger("code-427", "name-915")
        val view = toDefaultRouteOffsetLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRouteOffsetLedgerViews(mutableListOf<DefaultRouteOffsetLedger>(item, item)).size, "views")
    }
}
