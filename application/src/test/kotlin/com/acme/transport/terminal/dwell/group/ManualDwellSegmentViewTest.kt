package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDwellSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDwellSegment("code-207", "name-920")
        val view = toManualDwellSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDwellSegmentViews(mutableListOf<ManualDwellSegment>(item, item)).size, "views")
    }
}
