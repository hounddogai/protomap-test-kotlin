package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialBatchTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialBatch("code-375", "label-42")
        assertEquals(AccessorialBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAccessorialBatch("code-375", "label-42")
        try {
            item.moveTo(AccessorialBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialBatch("code-375", "label-42")
        item.status = AccessorialBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialBatchStatus.OPEN) }
    }
}
