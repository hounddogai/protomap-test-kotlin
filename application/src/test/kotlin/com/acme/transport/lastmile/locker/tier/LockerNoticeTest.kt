package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newLockerNotice("code-640", "name-282")
        assertEquals(LockerNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLockerNotice("code-640", "name-282")
        try {
            item.moveTo(LockerNoticeStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerNoticeStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLockerNotice("code-640", "name-282")
        item.status = LockerNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(LockerNoticeStatus.REQUESTED) }
    }
}
