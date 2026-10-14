package com.acme.transport.lastmile.locker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newLockerBatch("code-570", "label-921")
        assertEquals(LockerBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLockerBatch("code-570", "label-921")
        try {
            item.moveTo(LockerBatchStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerBatchStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLockerBatch("code-570", "label-921")
        item.status = LockerBatchStatus.CLOSED
        assertFails("move") { item.moveTo(LockerBatchStatus.DRAFT) }
    }
}
