package com.acme.transport.lastmile.drop.dropforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDropSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDropSegment("code-295", "name-977")
        assertEquals(RegionalDropSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRegionalDropSegment("code-295", "name-977")
        try {
            item.moveTo(RegionalDropSegmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDropSegmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDropSegment("code-295", "name-977")
        item.status = RegionalDropSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDropSegmentStatus.PLANNED) }
    }
}
