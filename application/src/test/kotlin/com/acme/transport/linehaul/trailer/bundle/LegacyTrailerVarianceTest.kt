package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTrailerVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyTrailerVariance("code-156", "label-790")
        assertEquals(LegacyTrailerVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyTrailerVariance("code-156", "label-790")
        try {
            item.moveTo(LegacyTrailerVarianceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTrailerVarianceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyTrailerVariance("code-156", "label-790")
        item.status = LegacyTrailerVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyTrailerVarianceStatus.PLANNED) }
    }
}
