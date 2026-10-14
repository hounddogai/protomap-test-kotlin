package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingHubLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingHubLedger("code-692", "name-235")
        val view = toRollingHubLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingHubLedgerViews(mutableListOf<RollingHubLedger>(item, item)).size, "views")
    }
}
