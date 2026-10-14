package com.acme.transport.fleetops.tire.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newTireSummary("code-874", "title-938")
        assertEquals(TireSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTireSummary("code-874", "title-938")
        try {
            item.moveTo(TireSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTireSummary("code-874", "title-938")
        item.status = TireSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(TireSummaryStatus.OPEN) }
    }
}
