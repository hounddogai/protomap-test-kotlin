package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTariffCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newRollingTariffCheckpoint("code-807", "title-381")
        assertEquals(RollingTariffCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingTariffCheckpoint("code-807", "title-381")
        try {
            item.moveTo(RollingTariffCheckpointStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTariffCheckpointStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingTariffCheckpoint("code-807", "title-381")
        item.status = RollingTariffCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingTariffCheckpointStatus.OPEN) }
    }
}
