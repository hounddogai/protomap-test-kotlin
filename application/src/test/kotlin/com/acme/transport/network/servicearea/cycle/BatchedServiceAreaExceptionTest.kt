package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServiceAreaExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedServiceAreaException("code-486", "title-570")
        assertEquals(BatchedServiceAreaExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedServiceAreaException("code-486", "title-570")
        try {
            item.moveTo(BatchedServiceAreaExceptionStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServiceAreaExceptionStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedServiceAreaException("code-486", "title-570")
        item.status = BatchedServiceAreaExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedServiceAreaExceptionStatus.DRAFT) }
    }
}
