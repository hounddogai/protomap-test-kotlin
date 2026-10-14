package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDefectVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredDefectVariance("code-890", "label-536")
        assertEquals(TieredDefectVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredDefectVariance("code-890", "label-536")
        try {
            item.moveTo(TieredDefectVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDefectVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredDefectVariance("code-890", "label-536")
        item.status = TieredDefectVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredDefectVarianceStatus.PLANNED) }
    }
}
