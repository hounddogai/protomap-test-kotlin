package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionRequest("code-991", "title-286")
        val view = toInspectionRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionRequestViews(mutableListOf<InspectionRequest>(item, item)).size, "views")
    }
}
