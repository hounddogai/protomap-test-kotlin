package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBackhaulCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingBackhaulCycle("code-539", "name-595")
        assertEquals(PendingBackhaulCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingBackhaulCycle("code-539", "name-595")
        try {
            item.moveTo(PendingBackhaulCycleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBackhaulCycleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingBackhaulCycle("code-539", "name-595")
        item.status = PendingBackhaulCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingBackhaulCycleStatus.REQUESTED) }
    }
}
