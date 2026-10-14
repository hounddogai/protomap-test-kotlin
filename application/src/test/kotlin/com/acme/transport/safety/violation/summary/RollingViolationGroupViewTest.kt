package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingViolationGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingViolationGroup("code-814", "label-475")
        val view = toRollingViolationGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingViolationGroupViews(mutableListOf<RollingViolationGroup>(item, item)).size, "views")
    }
}
