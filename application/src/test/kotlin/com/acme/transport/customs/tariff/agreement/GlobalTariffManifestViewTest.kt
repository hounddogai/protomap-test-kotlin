package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTariffManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTariffManifest("code-110", "name-491")
        val view = toGlobalTariffManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTariffManifestViews(mutableListOf<GlobalTariffManifest>(item, item)).size, "views")
    }
}
