package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledAccessorialBatch("code-278", "name-294")
        assertEquals(ScheduledAccessorialBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newScheduledAccessorialBatch("code-278", "name-294")
        try {
            item.moveTo(ScheduledAccessorialBatchStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialBatchStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledAccessorialBatch("code-278", "name-294")
        item.status = ScheduledAccessorialBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledAccessorialBatchStatus.PLANNED) }
    }
}
