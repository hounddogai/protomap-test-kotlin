package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelClaim("code-502", "name-428")
        val view = toParcelClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelClaimViews(mutableListOf<ParcelClaim>(item, item)).size, "views")
    }
}
