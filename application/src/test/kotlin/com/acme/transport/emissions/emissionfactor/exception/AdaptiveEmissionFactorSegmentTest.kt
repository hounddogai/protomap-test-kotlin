package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEmissionFactorSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveEmissionFactorSegment("code-10", "label-560")
        assertEquals(AdaptiveEmissionFactorSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAdaptiveEmissionFactorSegment("code-10", "label-560")
        try {
            item.moveTo(AdaptiveEmissionFactorSegmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEmissionFactorSegmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveEmissionFactorSegment("code-10", "label-560")
        item.status = AdaptiveEmissionFactorSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveEmissionFactorSegmentStatus.OPEN) }
    }
}
