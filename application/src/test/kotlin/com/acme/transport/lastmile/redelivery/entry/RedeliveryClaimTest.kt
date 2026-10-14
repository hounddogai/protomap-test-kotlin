package com.acme.transport.lastmile.redelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryClaimTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryClaim("code-77", "label-36")
        assertEquals(RedeliveryClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRedeliveryClaim("code-77", "label-36")
        try {
            item.moveTo(RedeliveryClaimStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryClaimStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryClaim("code-77", "label-36")
        item.status = RedeliveryClaimStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryClaimStatus.NEW) }
    }
}
