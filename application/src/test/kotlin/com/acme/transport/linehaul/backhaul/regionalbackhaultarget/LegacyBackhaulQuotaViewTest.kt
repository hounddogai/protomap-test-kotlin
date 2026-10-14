package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyBackhaulQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        val view = toLegacyBackhaulQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyBackhaulQuotaViews(mutableListOf<LegacyBackhaulQuota>(item, item)).size, "views")
    }
}
