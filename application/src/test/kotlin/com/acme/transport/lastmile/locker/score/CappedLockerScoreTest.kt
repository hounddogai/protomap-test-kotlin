package com.acme.transport.lastmile.locker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLockerScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedLockerScore("code-549", "label-591")
        assertEquals(CappedLockerScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCappedLockerScore("code-549", "label-591")
        try {
            item.moveTo(CappedLockerScoreStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLockerScoreStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedLockerScore("code-549", "label-591")
        item.status = CappedLockerScoreStatus.CLOSED
        assertFails("move") { item.moveTo(CappedLockerScoreStatus.DRAFT) }
    }
}
