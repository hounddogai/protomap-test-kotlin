package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCoachingSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledCoachingSummary("code-398", "name-839")
        assertEquals(ScheduledCoachingSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledCoachingSummary("code-398", "name-839")
        try {
            item.moveTo(ScheduledCoachingSummaryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCoachingSummaryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledCoachingSummary("code-398", "name-839")
        item.status = ScheduledCoachingSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledCoachingSummaryStatus.OPEN) }
    }
}
