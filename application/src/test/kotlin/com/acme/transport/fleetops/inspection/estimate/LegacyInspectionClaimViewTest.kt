package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionClaim("code-949", "title-797")
        val view = toLegacyInspectionClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionClaimViews(mutableListOf<LegacyInspectionClaim>(item, item)).size, "views")
    }
}
