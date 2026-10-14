package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryProfileTest {
    @Test
    fun `starts in open`() {
        val item = newRedeliveryProfile("code-273", "name-26")
        assertEquals(RedeliveryProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRedeliveryProfile("code-273", "name-26")
        try {
            item.moveTo(RedeliveryProfileStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryProfileStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRedeliveryProfile("code-273", "name-26")
        item.status = RedeliveryProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(RedeliveryProfileStatus.OPEN) }
    }
}
