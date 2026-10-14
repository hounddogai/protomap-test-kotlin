package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerDigest("code-205", "title-903")
        assertEquals(TrailerDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTrailerDigest("code-205", "title-903")
        try {
            item.moveTo(TrailerDigestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerDigestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerDigest("code-205", "title-903")
        item.status = TrailerDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerDigestStatus.PLANNED) }
    }
}
