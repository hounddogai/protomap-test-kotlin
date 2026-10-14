package com.acme.transport.lastmile.locker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newLockerBaseline("code-272", "title-178")
        assertEquals(LockerBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLockerBaseline("code-272", "title-178")
        try {
            item.moveTo(LockerBaselineStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerBaselineStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLockerBaseline("code-272", "title-178")
        item.status = LockerBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(LockerBaselineStatus.REQUESTED) }
    }
}
