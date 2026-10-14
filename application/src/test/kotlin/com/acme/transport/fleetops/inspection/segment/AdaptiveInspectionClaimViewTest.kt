package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionClaim("code-529", "title-715")
        val view = toAdaptiveInspectionClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionClaimViews(mutableListOf<AdaptiveInspectionClaim>(item, item)).size, "views")
    }
}
