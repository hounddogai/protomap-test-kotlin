package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffSummary("code-241", "label-168")
        assertEquals(CutoffSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffSummary("code-241", "label-168")
        try {
            item.moveTo(CutoffSummaryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffSummaryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffSummary("code-241", "label-168")
        item.status = CutoffSummaryStatus.FAILED
        assertFails("move") { item.moveTo(CutoffSummaryStatus.NEW) }
    }
}
