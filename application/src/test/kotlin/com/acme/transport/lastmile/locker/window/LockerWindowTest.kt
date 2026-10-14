package com.acme.transport.lastmile.locker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newLockerWindow("code-283", "name-692")
        assertEquals(LockerWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLockerWindow("code-283", "name-692")
        try {
            item.moveTo(LockerWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLockerWindow("code-283", "name-692")
        item.status = LockerWindowStatus.CLOSED
        assertFails("move") { item.moveTo(LockerWindowStatus.DRAFT) }
    }
}
