package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyZoneRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyZoneRecord("code-925", "title-330")
        val view = toLegacyZoneRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyZoneRecordViews(mutableListOf<LegacyZoneRecord>(item, item)).size, "views")
    }
}
