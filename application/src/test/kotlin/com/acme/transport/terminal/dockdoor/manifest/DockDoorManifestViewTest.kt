package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorManifest("code-258", "label-831")
        val view = toDockDoorManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorManifestViews(mutableListOf<DockDoorManifest>(item, item)).size, "views")
    }
}
