package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTenderClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTenderClaim("code-308", "name-936")
        val view = toCappedTenderClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTenderClaimViews(mutableListOf<CappedTenderClaim>(item, item)).size, "views")
    }
}
