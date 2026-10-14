package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTariffEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTariffEntry("code-454", "label-425")
        val view = toLenientTariffEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTariffEntryViews(mutableListOf<LenientTariffEntry>(item, item)).size, "views")
    }
}
