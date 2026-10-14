package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectException("code-851", "name-798")
        val view = toDefectExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectExceptionViews(mutableListOf<DefectException>(item, item)).size, "views")
    }
}
