package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyHubRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyHubRecord("code-452", "title-373")
        val view = toLegacyHubRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyHubRecordViews(mutableListOf<LegacyHubRecord>(item, item)).size, "views")
    }
}
