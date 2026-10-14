package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldManifest("code-482", "name-868")
        val view = toHoldManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldManifestViews(mutableListOf<HoldManifest>(item, item)).size, "views")
    }
}
