package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newRedeliveryTarget("code-716", "name-380")
        assertEquals(RedeliveryTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRedeliveryTarget("code-716", "name-380")
        try {
            item.moveTo(RedeliveryTargetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryTargetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRedeliveryTarget("code-716", "name-380")
        item.status = RedeliveryTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(RedeliveryTargetStatus.REQUESTED) }
    }
}
