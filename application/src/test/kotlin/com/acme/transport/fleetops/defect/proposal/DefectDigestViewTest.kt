package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectDigest("code-983", "label-797")
        val view = toDefectDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectDigestViews(mutableListOf<DefectDigest>(item, item)).size, "views")
    }
}
