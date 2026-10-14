package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterSnapshot("code-434", "name-755")
        val view = toRosterSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterSnapshotViews(mutableListOf<RosterSnapshot>(item, item)).size, "views")
    }
}
