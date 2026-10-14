package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newManualTrailerVariance("code-39", "name-175")
        assertEquals(ManualTrailerVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualTrailerVariance("code-39", "name-175")
        try {
            item.moveTo(ManualTrailerVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualTrailerVariance("code-39", "name-175")
        item.status = ManualTrailerVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualTrailerVarianceStatus.PLANNED) }
    }
}
