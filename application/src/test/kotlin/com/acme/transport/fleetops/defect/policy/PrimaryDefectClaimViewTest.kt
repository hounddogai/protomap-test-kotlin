package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDefectClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDefectClaim("code-401", "title-979")
        val view = toPrimaryDefectClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDefectClaimViews(mutableListOf<PrimaryDefectClaim>(item, item)).size, "views")
    }
}
