package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionManifest("code-391", "name-84")
        val view = toInspectionManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionManifestViews(mutableListOf<InspectionManifest>(item, item)).size, "views")
    }
}
