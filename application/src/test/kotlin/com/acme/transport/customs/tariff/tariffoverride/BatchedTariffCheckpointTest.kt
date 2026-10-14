package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTariffCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedTariffCheckpoint("code-298", "label-217")
        assertEquals(BatchedTariffCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedTariffCheckpoint("code-298", "label-217")
        try {
            item.moveTo(BatchedTariffCheckpointStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTariffCheckpointStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedTariffCheckpoint("code-298", "label-217")
        item.status = BatchedTariffCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedTariffCheckpointStatus.OPEN) }
    }
}
