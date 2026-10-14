package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerVariance("code-389", "title-59")
        assertEquals(TrailerVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTrailerVariance("code-389", "title-59")
        try {
            item.moveTo(TrailerVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerVariance("code-389", "title-59")
        item.status = TrailerVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerVarianceStatus.IDLE) }
    }
}
