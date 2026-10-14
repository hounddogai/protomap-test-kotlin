package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAttemptTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAttemptTier("code-932", "label-271")
        val view = toBatchedAttemptTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAttemptTierViews(mutableListOf<BatchedAttemptTier>(item, item)).size, "views")
    }
}
