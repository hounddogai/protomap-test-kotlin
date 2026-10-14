package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAttemptProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAttemptProfile("code-488", "title-737")
        val view = toWeightedAttemptProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAttemptProfileViews(mutableListOf<WeightedAttemptProfile>(item, item)).size, "views")
    }
}
