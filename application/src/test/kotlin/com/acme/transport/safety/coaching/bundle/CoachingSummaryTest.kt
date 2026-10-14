package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newCoachingSummary("code-840", "name-158")
        assertEquals(CoachingSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCoachingSummary("code-840", "name-158")
        try {
            item.moveTo(CoachingSummaryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingSummaryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCoachingSummary("code-840", "name-158")
        item.status = CoachingSummaryStatus.FAILED
        assertFails("move") { item.moveTo(CoachingSummaryStatus.NEW) }
    }
}
