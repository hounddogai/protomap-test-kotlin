package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTenderRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTenderRollup("code-868", "name-811")
        val view = toPrimaryTenderRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTenderRollupViews(mutableListOf<PrimaryTenderRollup>(item, item)).size, "views")
    }
}
