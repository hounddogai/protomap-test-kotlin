package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerRollupTest {
    @Test
    fun `starts in open`() {
        val item = newLockerRollup("code-507", "name-475")
        assertEquals(LockerRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLockerRollup("code-507", "name-475")
        try {
            item.moveTo(LockerRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLockerRollup("code-507", "name-475")
        item.status = LockerRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(LockerRollupStatus.OPEN) }
    }
}
