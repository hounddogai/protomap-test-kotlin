package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAttemptPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAttemptPlan("code-312", "title-159")
        val view = toStrictAttemptPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAttemptPlanViews(mutableListOf<StrictAttemptPlan>(item, item)).size, "views")
    }
}
