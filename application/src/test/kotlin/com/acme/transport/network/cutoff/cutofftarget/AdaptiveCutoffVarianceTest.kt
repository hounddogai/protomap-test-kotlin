package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCutoffVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveCutoffVariance("code-532", "title-127")
        assertEquals(AdaptiveCutoffVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveCutoffVariance("code-532", "title-127")
        try {
            item.moveTo(AdaptiveCutoffVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCutoffVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveCutoffVariance("code-532", "title-127")
        item.status = AdaptiveCutoffVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveCutoffVarianceStatus.PLANNED) }
    }
}
