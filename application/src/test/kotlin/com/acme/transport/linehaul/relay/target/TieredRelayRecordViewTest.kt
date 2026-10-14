package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredRelayRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredRelayRecord("code-813", "name-338")
        val view = toTieredRelayRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredRelayRecordViews(mutableListOf<TieredRelayRecord>(item, item)).size, "views")
    }
}
