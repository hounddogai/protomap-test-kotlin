package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffCheckpoint("code-481", "label-912")
        assertEquals(TariffCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTariffCheckpoint("code-481", "label-912")
        try {
            item.moveTo(TariffCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffCheckpoint("code-481", "label-912")
        item.status = TariffCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(TariffCheckpointStatus.IDLE) }
    }
}
