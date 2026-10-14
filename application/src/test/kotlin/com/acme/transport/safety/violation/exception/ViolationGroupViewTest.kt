package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationGroup("code-280", "title-692")
        val view = toViolationGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationGroupViews(mutableListOf<ViolationGroup>(item, item)).size, "views")
    }
}
