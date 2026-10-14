package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedEvChargeRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedEvChargeRecord("code-573", "label-633")
        val view = toCappedEvChargeRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedEvChargeRecordViews(mutableListOf<CappedEvChargeRecord>(item, item)).size, "views")
    }
}
