package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newHoldSummary("code-49", "name-451")
        assertEquals(HoldSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newHoldSummary("code-49", "name-451")
        try {
            item.moveTo(HoldSummaryStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldSummaryStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHoldSummary("code-49", "name-451")
        item.status = HoldSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(HoldSummaryStatus.OPEN) }
    }
}
