package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTenderRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTenderRollup("code-688", "title-515")
        val view = toManualTenderRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTenderRollupViews(mutableListOf<ManualTenderRollup>(item, item)).size, "views")
    }
}
