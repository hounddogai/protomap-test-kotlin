package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellThreshold("code-641", "label-555")
        val view = toDwellThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellThresholdViews(mutableListOf<DwellThreshold>(item, item)).size, "views")
    }
}
