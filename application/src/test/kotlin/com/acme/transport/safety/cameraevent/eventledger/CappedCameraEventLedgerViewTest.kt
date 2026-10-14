package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCameraEventLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCameraEventLedger("code-511", "name-584")
        val view = toCappedCameraEventLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCameraEventLedgerViews(mutableListOf<CappedCameraEventLedger>(item, item)).size, "views")
    }
}
