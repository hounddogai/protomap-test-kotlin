package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newLockerVariance("code-559", "title-268")
        assertEquals(LockerVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLockerVariance("code-559", "title-268")
        try {
            item.moveTo(LockerVarianceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerVarianceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLockerVariance("code-559", "title-268")
        item.status = LockerVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(LockerVarianceStatus.REQUESTED) }
    }
}
