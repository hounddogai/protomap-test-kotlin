package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffDraft("code-452", "name-111")
        val view = toTariffDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffDraftViews(mutableListOf<TariffDraft>(item, item)).size, "views")
    }
}
