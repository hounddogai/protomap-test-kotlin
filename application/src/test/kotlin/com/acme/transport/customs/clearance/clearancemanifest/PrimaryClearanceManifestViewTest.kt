package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryClearanceManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryClearanceManifest("code-36", "title-546")
        val view = toPrimaryClearanceManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryClearanceManifestViews(mutableListOf<PrimaryClearanceManifest>(item, item)).size, "views")
    }
}
