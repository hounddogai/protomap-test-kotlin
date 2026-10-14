package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationPreference("code-166", "title-661")
        val view = toViolationPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationPreferenceViews(mutableListOf<ViolationPreference>(item, item)).size, "views")
    }
}
