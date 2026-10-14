package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        assertEquals(EmissionFactorSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        try {
            item.moveTo(EmissionFactorSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        item.status = EmissionFactorSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorSnapshotStatus.NEW) }
    }
}
