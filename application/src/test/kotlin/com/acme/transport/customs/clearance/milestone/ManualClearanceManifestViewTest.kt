package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualClearanceManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualClearanceManifest("code-477", "name-793")
        val view = toManualClearanceManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualClearanceManifestViews(mutableListOf<ManualClearanceManifest>(item, item)).size, "views")
    }
}
