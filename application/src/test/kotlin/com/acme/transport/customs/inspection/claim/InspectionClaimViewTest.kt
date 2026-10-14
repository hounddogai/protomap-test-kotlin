package com.acme.transport.customs.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionClaim("code-861", "title-268")
        val view = toInspectionClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionClaimViews(mutableListOf<InspectionClaim>(item, item)).size, "views")
    }
}
