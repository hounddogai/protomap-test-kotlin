package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRestBreakEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingRestBreakEntry("code-46", "label-716")
        assertEquals(RollingRestBreakEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingRestBreakEntry("code-46", "label-716")
        try {
            item.moveTo(RollingRestBreakEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRestBreakEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingRestBreakEntry("code-46", "label-716")
        item.status = RollingRestBreakEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingRestBreakEntryStatus.REQUESTED) }
    }
}
