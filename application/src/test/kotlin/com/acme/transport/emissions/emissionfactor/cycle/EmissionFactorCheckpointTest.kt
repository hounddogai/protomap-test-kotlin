package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        assertEquals(EmissionFactorCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        try {
            item.moveTo(EmissionFactorCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        item.status = EmissionFactorCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorCheckpointStatus.REQUESTED) }
    }
}
