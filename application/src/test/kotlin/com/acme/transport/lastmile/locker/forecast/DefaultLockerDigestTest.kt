package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLockerDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultLockerDigest("code-142", "name-750")
        assertEquals(DefaultLockerDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultLockerDigest("code-142", "name-750")
        try {
            item.moveTo(DefaultLockerDigestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLockerDigestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultLockerDigest("code-142", "name-750")
        item.status = DefaultLockerDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultLockerDigestStatus.PROPOSED) }
    }
}
