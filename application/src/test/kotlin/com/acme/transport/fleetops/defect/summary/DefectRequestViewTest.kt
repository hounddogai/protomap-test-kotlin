package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectRequest("code-801", "name-662")
        val view = toDefectRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectRequestViews(mutableListOf<DefectRequest>(item, item)).size, "views")
    }
}
