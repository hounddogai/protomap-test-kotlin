package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeRecord("code-388", "label-694")
        val view = toEvChargeRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeRecordViews(mutableListOf<EvChargeRecord>(item, item)).size, "views")
    }
}
