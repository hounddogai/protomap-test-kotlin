package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingQualificationAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingQualificationAllocation("code-622", "title-345")
        val view = toRollingQualificationAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingQualificationAllocationViews(mutableListOf<RollingQualificationAllocation>(item, item)).size, "views")
    }
}
