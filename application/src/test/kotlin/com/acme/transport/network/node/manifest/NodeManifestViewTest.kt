package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeManifest("code-185", "title-965")
        val view = toNodeManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeManifestViews(mutableListOf<NodeManifest>(item, item)).size, "views")
    }
}
