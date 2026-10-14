package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRestBreakSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingRestBreakSummary("code-443", "name-142")
        assertEquals(RollingRestBreakSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRollingRestBreakSummary("code-443", "name-142")
        try {
            item.moveTo(RollingRestBreakSummaryStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRestBreakSummaryStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingRestBreakSummary("code-443", "name-142")
        item.status = RollingRestBreakSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingRestBreakSummaryStatus.PLANNED) }
    }
}
