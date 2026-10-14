package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictZoneRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictZoneRecord("code-389", "title-353")
        val view = toStrictZoneRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictZoneRecordViews(mutableListOf<StrictZoneRecord>(item, item)).size, "views")
    }
}
