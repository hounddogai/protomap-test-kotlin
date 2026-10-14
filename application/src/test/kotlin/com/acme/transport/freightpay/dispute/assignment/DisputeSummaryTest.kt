package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newDisputeSummary("code-505", "label-992")
        assertEquals(DisputeSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDisputeSummary("code-505", "label-992")
        try {
            item.moveTo(DisputeSummaryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeSummaryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDisputeSummary("code-505", "label-992")
        item.status = DisputeSummaryStatus.FAILED
        assertFails("move") { item.moveTo(DisputeSummaryStatus.NEW) }
    }
}
