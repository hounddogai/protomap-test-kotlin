package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newEvChargeBatch("code-217", "name-16")
        assertEquals(EvChargeBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newEvChargeBatch("code-217", "name-16")
        try {
            item.moveTo(EvChargeBatchStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeBatchStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEvChargeBatch("code-217", "name-16")
        item.status = EvChargeBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(EvChargeBatchStatus.PLANNED) }
    }
}
