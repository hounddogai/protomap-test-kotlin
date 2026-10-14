package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAttemptSlotTest {
    @Test
    fun `starts in created`() {
        val item = newCappedAttemptSlot("code-809", "name-905")
        assertEquals(CappedAttemptSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedAttemptSlot("code-809", "name-905")
        try {
            item.moveTo(CappedAttemptSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAttemptSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedAttemptSlot("code-809", "name-905")
        item.status = CappedAttemptSlotStatus.RETIRED
        assertFails("move") { item.moveTo(CappedAttemptSlotStatus.CREATED) }
    }
}
