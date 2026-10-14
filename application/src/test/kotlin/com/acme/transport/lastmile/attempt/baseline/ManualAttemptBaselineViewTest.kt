package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAttemptBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAttemptBaseline("code-895", "title-900")
        val view = toManualAttemptBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAttemptBaselineViews(mutableListOf<ManualAttemptBaseline>(item, item)).size, "views")
    }
}
