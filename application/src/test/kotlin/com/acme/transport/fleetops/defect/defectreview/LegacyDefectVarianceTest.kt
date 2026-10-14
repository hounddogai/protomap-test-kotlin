package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDefectVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyDefectVariance("code-222", "title-249")
        assertEquals(LegacyDefectVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLegacyDefectVariance("code-222", "title-249")
        try {
            item.moveTo(LegacyDefectVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDefectVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyDefectVariance("code-222", "title-249")
        item.status = LegacyDefectVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyDefectVarianceStatus.PLANNED) }
    }
}
