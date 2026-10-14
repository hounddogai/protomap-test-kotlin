package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryTierTest {
    @Test
    fun `starts in requested`() {
        val item = newRedeliveryTier("code-978", "title-879")
        assertEquals(RedeliveryTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRedeliveryTier("code-978", "title-879")
        try {
            item.moveTo(RedeliveryTierStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryTierStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRedeliveryTier("code-978", "title-879")
        item.status = RedeliveryTierStatus.EXPIRED
        assertFails("move") { item.moveTo(RedeliveryTierStatus.REQUESTED) }
    }
}
