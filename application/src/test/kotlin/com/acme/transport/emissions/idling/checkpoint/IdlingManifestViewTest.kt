package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingManifest("code-66", "title-10")
        val view = toIdlingManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingManifestViews(mutableListOf<IdlingManifest>(item, item)).size, "views")
    }
}
