package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerYardVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newRollingTrailerYardVariance("code-831", "name-415")
        assertEquals(RollingTrailerYardVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingTrailerYardVariance("code-831", "name-415")
        try {
            item.moveTo(RollingTrailerYardVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerYardVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingTrailerYardVariance("code-831", "name-415")
        item.status = RollingTrailerYardVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingTrailerYardVarianceStatus.OPEN) }
    }
}
