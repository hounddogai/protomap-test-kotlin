package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCapacityExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newManualCapacityException("code-670", "label-497")
        assertEquals(ManualCapacityExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualCapacityException("code-670", "label-497")
        try {
            item.moveTo(ManualCapacityExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCapacityExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualCapacityException("code-670", "label-497")
        item.status = ManualCapacityExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualCapacityExceptionStatus.PLANNED) }
    }
}
