package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliverySummaryTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliverySummary("code-395", "label-307")
        assertEquals(RedeliverySummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRedeliverySummary("code-395", "label-307")
        try {
            item.moveTo(RedeliverySummaryStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliverySummaryStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliverySummary("code-395", "label-307")
        item.status = RedeliverySummaryStatus.FAILED
        assertFails("move") { item.moveTo(RedeliverySummaryStatus.NEW) }
    }
}
