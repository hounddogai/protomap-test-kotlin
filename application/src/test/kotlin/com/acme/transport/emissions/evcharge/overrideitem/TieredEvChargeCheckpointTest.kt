package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEvChargeCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newTieredEvChargeCheckpoint("code-135", "name-202")
        assertEquals(TieredEvChargeCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredEvChargeCheckpoint("code-135", "name-202")
        try {
            item.moveTo(TieredEvChargeCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEvChargeCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredEvChargeCheckpoint("code-135", "name-202")
        item.status = TieredEvChargeCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(TieredEvChargeCheckpointStatus.CREATED) }
    }
}
