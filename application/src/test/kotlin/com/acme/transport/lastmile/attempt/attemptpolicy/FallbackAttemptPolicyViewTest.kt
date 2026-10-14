package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAttemptPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAttemptPolicy("code-545", "name-565")
        val view = toFallbackAttemptPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAttemptPolicyViews(mutableListOf<FallbackAttemptPolicy>(item, item)).size, "views")
    }
}
