package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDisputeSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newManualDisputeSummary("code-390", "label-408")
        assertEquals(ManualDisputeSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualDisputeSummary("code-390", "label-408")
        try {
            item.moveTo(ManualDisputeSummaryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDisputeSummaryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualDisputeSummary("code-390", "label-408")
        item.status = ManualDisputeSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(ManualDisputeSummaryStatus.IDLE) }
    }
}
