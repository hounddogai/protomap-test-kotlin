package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingVariance("code-285", "name-537")
        val view = toCoachingVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingVarianceViews(mutableListOf<CoachingVariance>(item, item)).size, "views")
    }
}
