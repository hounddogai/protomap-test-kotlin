package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderClaim("code-48", "name-63")
        val view = toTenderClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderClaimViews(mutableListOf<TenderClaim>(item, item)).size, "views")
    }
}
