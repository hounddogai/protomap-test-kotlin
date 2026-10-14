package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDisputeGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictDisputeGroup("code-426", "name-991")
        assertEquals(StrictDisputeGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictDisputeGroup("code-426", "name-991")
        try {
            item.moveTo(StrictDisputeGroupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDisputeGroupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictDisputeGroup("code-426", "name-991")
        item.status = StrictDisputeGroupStatus.STOPPED
        assertFails("move") { item.moveTo(StrictDisputeGroupStatus.IDLE) }
    }
}
