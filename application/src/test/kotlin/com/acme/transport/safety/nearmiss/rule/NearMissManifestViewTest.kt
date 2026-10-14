package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissManifest("code-431", "name-836")
        val view = toNearMissManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissManifestViews(mutableListOf<NearMissManifest>(item, item)).size, "views")
    }
}
