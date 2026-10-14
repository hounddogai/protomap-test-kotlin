package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledHoldSummary("code-93", "name-655")
        assertEquals(ScheduledHoldSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledHoldSummary("code-93", "name-655")
        try {
            item.moveTo(ScheduledHoldSummaryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldSummaryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledHoldSummary("code-93", "name-655")
        item.status = ScheduledHoldSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledHoldSummaryStatus.OPEN) }
    }
}
