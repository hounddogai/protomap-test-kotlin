package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingViolationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingViolationWindow("code-204", "name-82")
        val view = toRollingViolationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingViolationWindowViews(mutableListOf<RollingViolationWindow>(item, item)).size, "views")
    }
}
