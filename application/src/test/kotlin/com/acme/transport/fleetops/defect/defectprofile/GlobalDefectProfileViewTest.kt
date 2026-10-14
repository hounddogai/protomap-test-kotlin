package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDefectProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDefectProfile("code-310", "name-460")
        val view = toGlobalDefectProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDefectProfileViews(mutableListOf<GlobalDefectProfile>(item, item)).size, "views")
    }
}
