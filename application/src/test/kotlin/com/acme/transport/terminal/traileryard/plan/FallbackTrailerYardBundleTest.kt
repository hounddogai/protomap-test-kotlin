package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTrailerYardBundleTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackTrailerYardBundle("code-11", "name-153")
        assertEquals(FallbackTrailerYardBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackTrailerYardBundle("code-11", "name-153")
        try {
            item.moveTo(FallbackTrailerYardBundleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTrailerYardBundleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackTrailerYardBundle("code-11", "name-153")
        item.status = FallbackTrailerYardBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackTrailerYardBundleStatus.OPEN) }
    }
}
