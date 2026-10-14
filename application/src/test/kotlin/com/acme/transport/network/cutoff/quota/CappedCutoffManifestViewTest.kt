package com.acme.transport.network.cutoff.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCutoffManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCutoffManifest("code-532", "name-353")
        val view = toCappedCutoffManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCutoffManifestViews(mutableListOf<CappedCutoffManifest>(item, item)).size, "views")
    }
}
