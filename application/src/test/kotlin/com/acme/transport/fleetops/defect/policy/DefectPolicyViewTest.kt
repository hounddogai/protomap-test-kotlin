package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectPolicy("code-424", "title-366")
        val view = toDefectPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectPolicyViews(mutableListOf<DefectPolicy>(item, item)).size, "views")
    }
}
