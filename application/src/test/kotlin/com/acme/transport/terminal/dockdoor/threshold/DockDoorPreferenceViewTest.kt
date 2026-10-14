package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorPreference("code-219", "label-793")
        val view = toDockDoorPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorPreferenceViews(mutableListOf<DockDoorPreference>(item, item)).size, "views")
    }
}
