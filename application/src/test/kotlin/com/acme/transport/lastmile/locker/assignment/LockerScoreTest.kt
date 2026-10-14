package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerScoreTest {
    @Test
    fun `starts in new`() {
        val item = newLockerScore("code-210", "title-498")
        assertEquals(LockerScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLockerScore("code-210", "title-498")
        try {
            item.moveTo(LockerScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLockerScore("code-210", "title-498")
        item.status = LockerScoreStatus.FAILED
        assertFails("move") { item.moveTo(LockerScoreStatus.NEW) }
    }
}
