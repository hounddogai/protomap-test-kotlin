package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEmissionFactorSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultEmissionFactorSegment("code-851", "label-140")
        assertEquals(DefaultEmissionFactorSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultEmissionFactorSegment("code-851", "label-140")
        try {
            item.moveTo(DefaultEmissionFactorSegmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEmissionFactorSegmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultEmissionFactorSegment("code-851", "label-140")
        item.status = DefaultEmissionFactorSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultEmissionFactorSegmentStatus.REQUESTED) }
    }
}
