package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newTenderSummary("code-499", "title-471")
        assertEquals(TenderSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTenderSummary("code-499", "title-471")
        try {
            item.moveTo(TenderSummaryStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderSummaryStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTenderSummary("code-499", "title-471")
        item.status = TenderSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(TenderSummaryStatus.DRAFT) }
    }
}
