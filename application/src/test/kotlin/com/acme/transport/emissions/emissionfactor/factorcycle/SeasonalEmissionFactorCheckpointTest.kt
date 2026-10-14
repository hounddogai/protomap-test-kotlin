package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalEmissionFactorCheckpoint("code-316", "label-561")
        assertEquals(SeasonalEmissionFactorCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalEmissionFactorCheckpoint("code-316", "label-561")
        try {
            item.moveTo(SeasonalEmissionFactorCheckpointStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorCheckpointStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalEmissionFactorCheckpoint("code-316", "label-561")
        item.status = SeasonalEmissionFactorCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalEmissionFactorCheckpointStatus.OPEN) }
    }
}
