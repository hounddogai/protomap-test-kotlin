package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterCheckpoint("code-127", "name-664")
        val view = toRosterCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterCheckpointViews(mutableListOf<RosterCheckpoint>(item, item)).size, "views")
    }
}
