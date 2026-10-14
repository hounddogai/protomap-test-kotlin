package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLockerProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyLockerProfile("code-807", "name-643")
        assertEquals(LegacyLockerProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLegacyLockerProfile("code-807", "name-643")
        try {
            item.moveTo(LegacyLockerProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLockerProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyLockerProfile("code-807", "name-643")
        item.status = LegacyLockerProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyLockerProfileStatus.DRAFT) }
    }
}
