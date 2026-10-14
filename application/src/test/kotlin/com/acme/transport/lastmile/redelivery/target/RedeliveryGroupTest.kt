package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newRedeliveryGroup("code-355", "name-175")
        assertEquals(RedeliveryGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRedeliveryGroup("code-355", "name-175")
        try {
            item.moveTo(RedeliveryGroupStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryGroupStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRedeliveryGroup("code-355", "name-175")
        item.status = RedeliveryGroupStatus.CLOSED
        assertFails("move") { item.moveTo(RedeliveryGroupStatus.DRAFT) }
    }
}
