package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellLedger("code-648", "name-619")
        val view = toDwellLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellLedgerViews(mutableListOf<DwellLedger>(item, item)).size, "views")
    }
}
