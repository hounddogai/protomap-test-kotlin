package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalEmissionFactorSegment("code-475", "name-918")
        assertEquals(SeasonalEmissionFactorSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalEmissionFactorSegment("code-475", "name-918")
        try {
            item.moveTo(SeasonalEmissionFactorSegmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorSegmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalEmissionFactorSegment("code-475", "name-918")
        item.status = SeasonalEmissionFactorSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalEmissionFactorSegmentStatus.DRAFT) }
    }
}
