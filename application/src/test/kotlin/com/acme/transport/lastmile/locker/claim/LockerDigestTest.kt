package com.acme.transport.lastmile.locker.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerDigestTest {
    @Test
    fun `starts in created`() {
        val item = newLockerDigest("code-654", "title-112")
        assertEquals(LockerDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerDigest("code-654", "title-112")
        try {
            item.moveTo(LockerDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerDigest("code-654", "title-112")
        item.status = LockerDigestStatus.RETIRED
        assertFails("move") { item.moveTo(LockerDigestStatus.CREATED) }
    }
}
