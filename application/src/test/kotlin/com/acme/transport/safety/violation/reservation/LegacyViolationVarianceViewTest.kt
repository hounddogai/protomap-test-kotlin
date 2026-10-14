package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyViolationVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyViolationVariance("code-746", "title-339")
        val view = toLegacyViolationVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyViolationVarianceViews(mutableListOf<LegacyViolationVariance>(item, item)).size, "views")
    }
}
