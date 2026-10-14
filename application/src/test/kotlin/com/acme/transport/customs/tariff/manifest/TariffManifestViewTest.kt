package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffManifest("code-317", "name-817")
        val view = toTariffManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffManifestViews(mutableListOf<TariffManifest>(item, item)).size, "views")
    }
}
