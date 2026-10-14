package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTariffCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newManualTariffCheckpoint("code-636", "title-542")
        assertEquals(ManualTariffCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newManualTariffCheckpoint("code-636", "title-542")
        try {
            item.moveTo(ManualTariffCheckpointStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTariffCheckpointStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualTariffCheckpoint("code-636", "title-542")
        item.status = ManualTariffCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(ManualTariffCheckpointStatus.DRAFT) }
    }
}
