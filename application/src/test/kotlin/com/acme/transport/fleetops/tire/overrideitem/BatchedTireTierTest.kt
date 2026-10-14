package com.acme.transport.fleetops.tire.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTireTierTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedTireTier("code-163", "name-36")
        assertEquals(BatchedTireTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newBatchedTireTier("code-163", "name-36")
        try {
            item.moveTo(BatchedTireTierStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTireTierStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedTireTier("code-163", "name-36")
        item.status = BatchedTireTierStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedTireTierStatus.CREATED) }
    }
}
