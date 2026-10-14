package com.acme.transport.crewing.roster.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterThreshold("code-635", "title-76")
        val view = toRosterThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterThresholdViews(mutableListOf<RosterThreshold>(item, item)).size, "views")
    }
}
