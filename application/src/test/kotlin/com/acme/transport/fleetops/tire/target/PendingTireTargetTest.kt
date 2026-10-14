package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTireTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingTireTarget("code-513", "label-327")
        assertEquals(PendingTireTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingTireTarget("code-513", "label-327")
        try {
            item.moveTo(PendingTireTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTireTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingTireTarget("code-513", "label-327")
        item.status = PendingTireTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingTireTargetStatus.PLANNED) }
    }
}
