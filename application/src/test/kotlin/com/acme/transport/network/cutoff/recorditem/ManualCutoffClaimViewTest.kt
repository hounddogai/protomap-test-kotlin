package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCutoffClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCutoffClaim("code-894", "label-92")
        val view = toManualCutoffClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCutoffClaimViews(mutableListOf<ManualCutoffClaim>(item, item)).size, "views")
    }
}
