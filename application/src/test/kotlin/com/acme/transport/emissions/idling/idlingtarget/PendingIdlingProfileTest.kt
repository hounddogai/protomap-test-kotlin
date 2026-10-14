package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingIdlingProfileTest {
    @Test
    fun `starts in new`() {
        val item = newPendingIdlingProfile("code-930", "name-629")
        assertEquals(PendingIdlingProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPendingIdlingProfile("code-930", "name-629")
        try {
            item.moveTo(PendingIdlingProfileStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingIdlingProfileStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingIdlingProfile("code-930", "name-629")
        item.status = PendingIdlingProfileStatus.FAILED
        assertFails("move") { item.moveTo(PendingIdlingProfileStatus.NEW) }
    }
}
