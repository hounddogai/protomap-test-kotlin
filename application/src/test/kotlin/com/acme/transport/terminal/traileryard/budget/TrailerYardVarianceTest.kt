package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerYardVariance("code-934", "name-461")
        assertEquals(TrailerYardVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTrailerYardVariance("code-934", "name-461")
        try {
            item.moveTo(TrailerYardVarianceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardVarianceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerYardVariance("code-934", "name-461")
        item.status = TrailerYardVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerYardVarianceStatus.OPEN) }
    }
}
