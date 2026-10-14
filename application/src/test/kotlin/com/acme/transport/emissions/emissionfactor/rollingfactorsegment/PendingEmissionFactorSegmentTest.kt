package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEmissionFactorSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingEmissionFactorSegment("code-820", "title-727")
        assertEquals(PendingEmissionFactorSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingEmissionFactorSegment("code-820", "title-727")
        try {
            item.moveTo(PendingEmissionFactorSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEmissionFactorSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingEmissionFactorSegment("code-820", "title-727")
        item.status = PendingEmissionFactorSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingEmissionFactorSegmentStatus.PLANNED) }
    }
}
