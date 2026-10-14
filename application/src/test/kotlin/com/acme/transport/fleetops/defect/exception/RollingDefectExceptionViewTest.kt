package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDefectExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDefectException("code-599", "title-391")
        val view = toRollingDefectExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDefectExceptionViews(mutableListOf<RollingDefectException>(item, item)).size, "views")
    }
}
