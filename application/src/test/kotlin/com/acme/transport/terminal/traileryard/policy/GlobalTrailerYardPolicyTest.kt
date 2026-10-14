package com.acme.transport.terminal.traileryard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerYardPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalTrailerYardPolicy("code-513", "label-819")
        assertEquals(GlobalTrailerYardPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalTrailerYardPolicy("code-513", "label-819")
        try {
            item.moveTo(GlobalTrailerYardPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerYardPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalTrailerYardPolicy("code-513", "label-819")
        item.status = GlobalTrailerYardPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalTrailerYardPolicyStatus.IDLE) }
    }
}
