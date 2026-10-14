package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCrossDockRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCrossDockRecord("code-943", "title-260")
        val view = toScheduledCrossDockRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCrossDockRecordViews(mutableListOf<ScheduledCrossDockRecord>(item, item)).size, "views")
    }
}
