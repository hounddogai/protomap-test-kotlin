package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDisputeSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newStrictDisputeSummary("code-646", "name-976")
        assertEquals(StrictDisputeSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictDisputeSummary("code-646", "name-976")
        try {
            item.moveTo(StrictDisputeSummaryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDisputeSummaryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictDisputeSummary("code-646", "name-976")
        item.status = StrictDisputeSummaryStatus.FAILED
        assertFails("move") { item.moveTo(StrictDisputeSummaryStatus.NEW) }
    }
}
