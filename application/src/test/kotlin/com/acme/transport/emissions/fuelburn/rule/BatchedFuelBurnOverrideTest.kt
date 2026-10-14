package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelBurnOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedFuelBurnOverride("code-46", "name-659")
        assertEquals(BatchedFuelBurnOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedFuelBurnOverride("code-46", "name-659")
        try {
            item.moveTo(BatchedFuelBurnOverrideStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelBurnOverrideStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedFuelBurnOverride("code-46", "name-659")
        item.status = BatchedFuelBurnOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedFuelBurnOverrideStatus.CREATED) }
    }
}
