package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTariffDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTariffDraft("code-332", "name-449")
        val view = toRollingTariffDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTariffDraftViews(mutableListOf<RollingTariffDraft>(item, item)).size, "views")
    }
}
