package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCutoffClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCutoffClaim("code-71", "name-232")
        val view = toDefaultCutoffClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCutoffClaimViews(mutableListOf<DefaultCutoffClaim>(item, item)).size, "views")
    }
}
