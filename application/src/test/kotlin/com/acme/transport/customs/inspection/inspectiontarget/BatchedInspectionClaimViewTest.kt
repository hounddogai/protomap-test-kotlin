package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedInspectionClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedInspectionClaim("code-244", "name-284")
        val view = toBatchedInspectionClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedInspectionClaimViews(mutableListOf<BatchedInspectionClaim>(item, item)).size, "views")
    }
}
