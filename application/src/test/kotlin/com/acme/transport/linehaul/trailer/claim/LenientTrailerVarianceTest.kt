package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTrailerVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientTrailerVariance("code-511", "name-399")
        assertEquals(LenientTrailerVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientTrailerVariance("code-511", "name-399")
        try {
            item.moveTo(LenientTrailerVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTrailerVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientTrailerVariance("code-511", "name-399")
        item.status = LenientTrailerVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(LenientTrailerVarianceStatus.IDLE) }
    }
}
