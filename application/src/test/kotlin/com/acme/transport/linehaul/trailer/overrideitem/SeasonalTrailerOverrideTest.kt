package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalTrailerOverride("code-994", "label-43")
        assertEquals(SeasonalTrailerOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalTrailerOverride("code-994", "label-43")
        try {
            item.moveTo(SeasonalTrailerOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalTrailerOverride("code-994", "label-43")
        item.status = SeasonalTrailerOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalTrailerOverrideStatus.OPEN) }
    }
}
