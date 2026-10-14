package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDefectVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDefectVariance("code-730", "label-473")
        val view = toFallbackDefectVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDefectVarianceViews(mutableListOf<FallbackDefectVariance>(item, item)).size, "views")
    }
}
