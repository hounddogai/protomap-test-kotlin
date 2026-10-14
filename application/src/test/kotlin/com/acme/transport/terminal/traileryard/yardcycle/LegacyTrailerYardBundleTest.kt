package com.acme.transport.terminal.traileryard.yardcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTrailerYardBundleTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyTrailerYardBundle("code-102", "name-318")
        assertEquals(LegacyTrailerYardBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLegacyTrailerYardBundle("code-102", "name-318")
        try {
            item.moveTo(LegacyTrailerYardBundleStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTrailerYardBundleStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyTrailerYardBundle("code-102", "name-318")
        item.status = LegacyTrailerYardBundleStatus.FAILED
        assertFails("move") { item.moveTo(LegacyTrailerYardBundleStatus.NEW) }
    }
}
