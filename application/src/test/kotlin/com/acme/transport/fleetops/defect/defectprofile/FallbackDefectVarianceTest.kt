package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDefectVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDefectVariance("code-730", "label-473")
        assertEquals(FallbackDefectVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackDefectVariance("code-730", "label-473")
        try {
            item.moveTo(FallbackDefectVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDefectVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDefectVariance("code-730", "label-473")
        item.status = FallbackDefectVarianceStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDefectVarianceStatus.NEW) }
    }
}
