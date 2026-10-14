package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderManifest("code-579", "name-247")
        val view = toTenderManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderManifestViews(mutableListOf<TenderManifest>(item, item)).size, "views")
    }
}
