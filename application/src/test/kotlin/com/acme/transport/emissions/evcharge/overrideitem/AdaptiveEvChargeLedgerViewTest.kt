package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEvChargeLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEvChargeLedger("code-453", "title-789")
        val view = toAdaptiveEvChargeLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEvChargeLedgerViews(mutableListOf<AdaptiveEvChargeLedger>(item, item)).size, "views")
    }
}
