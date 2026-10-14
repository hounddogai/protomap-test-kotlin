package com.acme.transport.network.transittime.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimePolicyTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        assertEquals(DefaultTransitTimePolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        try {
            item.moveTo(DefaultTransitTimePolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimePolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        item.status = DefaultTransitTimePolicyStatus.FAILED
        assertFails("move") { item.moveTo(DefaultTransitTimePolicyStatus.NEW) }
    }
}
