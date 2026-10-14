package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEmissionFactorSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredEmissionFactorSegment("code-363", "name-63")
        assertEquals(TieredEmissionFactorSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTieredEmissionFactorSegment("code-363", "name-63")
        try {
            item.moveTo(TieredEmissionFactorSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEmissionFactorSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredEmissionFactorSegment("code-363", "name-63")
        item.status = TieredEmissionFactorSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredEmissionFactorSegmentStatus.PLANNED) }
    }
}
