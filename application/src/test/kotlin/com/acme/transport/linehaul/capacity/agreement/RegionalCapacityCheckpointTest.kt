package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalCapacityCheckpoint("code-9", "name-927")
        assertEquals(RegionalCapacityCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalCapacityCheckpoint("code-9", "name-927")
        try {
            item.moveTo(RegionalCapacityCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalCapacityCheckpoint("code-9", "name-927")
        item.status = RegionalCapacityCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalCapacityCheckpointStatus.PROPOSED) }
    }
}
