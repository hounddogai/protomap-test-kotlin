package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingViolationRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingViolationRollup("code-422", "name-880")
        assertEquals(PendingViolationRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPendingViolationRollup("code-422", "name-880")
        try {
            item.moveTo(PendingViolationRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingViolationRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingViolationRollup("code-422", "name-880")
        item.status = PendingViolationRollupStatus.STOPPED
        assertFails("move") { item.moveTo(PendingViolationRollupStatus.IDLE) }
    }
}
