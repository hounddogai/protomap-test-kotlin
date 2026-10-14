package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRestBreakVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        assertEquals(DefaultRestBreakVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        try {
            item.moveTo(DefaultRestBreakVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRestBreakVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        item.status = DefaultRestBreakVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultRestBreakVarianceStatus.OPEN) }
    }
}
