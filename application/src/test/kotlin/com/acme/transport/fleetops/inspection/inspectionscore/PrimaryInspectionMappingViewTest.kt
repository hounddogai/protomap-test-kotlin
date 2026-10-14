package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryInspectionMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryInspectionMapping("code-730", "label-319")
        val view = toPrimaryInspectionMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryInspectionMappingViews(mutableListOf<PrimaryInspectionMapping>(item, item)).size, "views")
    }
}
