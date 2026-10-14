package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCutoffClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCutoffClaim("code-582", "label-801")
        val view = toFallbackCutoffClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCutoffClaimViews(mutableListOf<FallbackCutoffClaim>(item, item)).size, "views")
    }
}
