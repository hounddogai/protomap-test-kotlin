package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorBatch("code-832", "label-329")
        assertEquals(EmissionFactorBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newEmissionFactorBatch("code-832", "label-329")
        try {
            item.moveTo(EmissionFactorBatchStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorBatchStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorBatch("code-832", "label-329")
        item.status = EmissionFactorBatchStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorBatchStatus.IDLE) }
    }
}
