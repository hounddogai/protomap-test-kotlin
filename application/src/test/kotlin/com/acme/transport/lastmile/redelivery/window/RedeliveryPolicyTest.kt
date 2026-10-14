package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryPolicy("code-632", "title-166")
        assertEquals(RedeliveryPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRedeliveryPolicy("code-632", "title-166")
        try {
            item.moveTo(RedeliveryPolicyStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryPolicyStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryPolicy("code-632", "title-166")
        item.status = RedeliveryPolicyStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryPolicyStatus.NEW) }
    }
}
