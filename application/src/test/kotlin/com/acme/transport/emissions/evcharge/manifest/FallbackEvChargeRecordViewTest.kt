package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEvChargeRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        val view = toFallbackEvChargeRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEvChargeRecordViews(mutableListOf<FallbackEvChargeRecord>(item, item)).size, "views")
    }
}
