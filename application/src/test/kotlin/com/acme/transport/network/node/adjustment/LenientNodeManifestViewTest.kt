package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNodeManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNodeManifest("code-783", "name-868")
        val view = toLenientNodeManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNodeManifestViews(mutableListOf<LenientNodeManifest>(item, item)).size, "views")
    }
}
