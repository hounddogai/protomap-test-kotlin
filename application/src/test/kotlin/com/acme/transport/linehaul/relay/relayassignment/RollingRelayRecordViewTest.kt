package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRelayRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRelayRecord("code-848", "title-43")
        val view = toRollingRelayRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRelayRecordViews(mutableListOf<RollingRelayRecord>(item, item)).size, "views")
    }
}
