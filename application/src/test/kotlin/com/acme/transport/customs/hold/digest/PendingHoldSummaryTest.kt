package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHoldSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingHoldSummary("code-681", "label-4")
        assertEquals(PendingHoldSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingHoldSummary("code-681", "label-4")
        try {
            item.moveTo(PendingHoldSummaryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHoldSummaryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingHoldSummary("code-681", "label-4")
        item.status = PendingHoldSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingHoldSummaryStatus.REQUESTED) }
    }
}
