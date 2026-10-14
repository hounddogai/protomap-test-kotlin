package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryViolationPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryViolationPreference("code-832", "title-534")
        val view = toPrimaryViolationPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryViolationPreferenceViews(mutableListOf<PrimaryViolationPreference>(item, item)).size, "views")
    }
}
