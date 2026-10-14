package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerSummary("code-842", "name-28")
        assertEquals(TrailerSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTrailerSummary("code-842", "name-28")
        try {
            item.moveTo(TrailerSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerSummary("code-842", "name-28")
        item.status = TrailerSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerSummaryStatus.PLANNED) }
    }
}
