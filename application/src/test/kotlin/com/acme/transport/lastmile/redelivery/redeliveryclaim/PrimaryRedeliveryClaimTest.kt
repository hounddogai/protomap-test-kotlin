package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryRedeliveryClaim("code-85", "label-204")
        assertEquals(PrimaryRedeliveryClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPrimaryRedeliveryClaim("code-85", "label-204")
        try {
            item.moveTo(PrimaryRedeliveryClaimStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryClaimStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryRedeliveryClaim("code-85", "label-204")
        item.status = PrimaryRedeliveryClaimStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryRedeliveryClaimStatus.DRAFT) }
    }
}
