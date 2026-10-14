package com.acme.transport.lastmile.drop.weighteddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newDropSummary("code-667", "title-503")
        assertEquals(DropSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDropSummary("code-667", "title-503")
        try {
            item.moveTo(DropSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDropSummary("code-667", "title-503")
        item.status = DropSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(DropSummaryStatus.PLANNED) }
    }
}
