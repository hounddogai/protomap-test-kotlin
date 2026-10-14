package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeTarget("code-949", "label-159")
        assertEquals(TransitTimeTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTransitTimeTarget("code-949", "label-159")
        try {
            item.moveTo(TransitTimeTargetStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeTargetStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeTarget("code-949", "label-159")
        item.status = TransitTimeTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeTargetStatus.REQUESTED) }
    }
}
