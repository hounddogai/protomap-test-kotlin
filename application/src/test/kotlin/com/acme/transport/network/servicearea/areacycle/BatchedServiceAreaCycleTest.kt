package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServiceAreaCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedServiceAreaCycle("code-21", "name-927")
        assertEquals(BatchedServiceAreaCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedServiceAreaCycle("code-21", "name-927")
        try {
            item.moveTo(BatchedServiceAreaCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServiceAreaCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedServiceAreaCycle("code-21", "name-927")
        item.status = BatchedServiceAreaCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedServiceAreaCycleStatus.REQUESTED) }
    }
}
