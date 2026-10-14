package com.acme.transport.emissions.emissionfactor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newEmissionFactorSegment("code-167", "name-928")
        assertEquals(EmissionFactorSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEmissionFactorSegment("code-167", "name-928")
        try {
            item.moveTo(EmissionFactorSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEmissionFactorSegment("code-167", "name-928")
        item.status = EmissionFactorSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(EmissionFactorSegmentStatus.OPEN) }
    }
}
