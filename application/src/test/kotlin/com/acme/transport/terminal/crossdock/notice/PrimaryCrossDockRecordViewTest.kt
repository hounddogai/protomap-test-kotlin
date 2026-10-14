package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCrossDockRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCrossDockRecord("code-229", "label-977")
        val view = toPrimaryCrossDockRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCrossDockRecordViews(mutableListOf<PrimaryCrossDockRecord>(item, item)).size, "views")
    }
}
