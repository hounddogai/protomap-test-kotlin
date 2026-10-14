package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNearMissClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNearMissClaim("code-534", "title-762")
        val view = toLenientNearMissClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNearMissClaimViews(mutableListOf<LenientNearMissClaim>(item, item)).size, "views")
    }
}
