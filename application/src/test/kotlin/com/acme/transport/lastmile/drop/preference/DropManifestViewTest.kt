package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class DropManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropManifest("code-919", "label-480")
        val view = toDropManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropManifestViews(mutableListOf<DropManifest>(item, item)).size, "views")
    }
}
