package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        assertEquals(BatchedRestBreakSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        try {
            item.moveTo(BatchedRestBreakSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        item.status = BatchedRestBreakSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedRestBreakSummaryStatus.PROPOSED) }
    }
}
