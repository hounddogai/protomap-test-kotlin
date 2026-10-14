package com.acme.transport.fleetops.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionDigest("code-668", "label-459")
        val view = toInspectionDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionDigestViews(mutableListOf<InspectionDigest>(item, item)).size, "views")
    }
}
