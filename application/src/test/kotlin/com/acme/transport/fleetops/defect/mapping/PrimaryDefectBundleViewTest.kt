package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDefectBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDefectBundle("code-210", "title-70")
        val view = toPrimaryDefectBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDefectBundleViews(mutableListOf<PrimaryDefectBundle>(item, item)).size, "views")
    }
}
