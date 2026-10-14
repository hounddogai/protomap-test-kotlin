package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualViolationSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newManualViolationSummary("code-76", "name-117")
        assertEquals(ManualViolationSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualViolationSummary("code-76", "name-117")
        try {
            item.moveTo(ManualViolationSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualViolationSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualViolationSummary("code-76", "name-117")
        item.status = ManualViolationSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualViolationSummaryStatus.PLANNED) }
    }
}
