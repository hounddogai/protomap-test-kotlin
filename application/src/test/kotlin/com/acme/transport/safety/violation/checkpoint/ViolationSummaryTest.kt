package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationSummary("code-790", "name-380")
        assertEquals(ViolationSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationSummary("code-790", "name-380")
        try {
            item.moveTo(ViolationSummaryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationSummaryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationSummary("code-790", "name-380")
        item.status = ViolationSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationSummaryStatus.REQUESTED) }
    }
}
