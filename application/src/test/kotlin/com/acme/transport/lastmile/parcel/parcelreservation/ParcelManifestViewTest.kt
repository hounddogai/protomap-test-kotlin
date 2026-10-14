package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelManifest("code-95", "label-844")
        val view = toParcelManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelManifestViews(mutableListOf<ParcelManifest>(item, item)).size, "views")
    }
}
