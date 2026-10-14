package com.acme.transport.customs.tariff.tariffdraft

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffRequest("code-325", "title-144")
        val view = toTariffRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffRequestViews(mutableListOf<TariffRequest>(item, item)).size, "views")
    }
}
