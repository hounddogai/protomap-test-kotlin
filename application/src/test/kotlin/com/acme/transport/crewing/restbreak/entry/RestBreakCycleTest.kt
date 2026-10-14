package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakCycleTest {
    @Test
    fun `starts in open`() {
        val item = newRestBreakCycle("code-211", "title-28")
        assertEquals(RestBreakCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRestBreakCycle("code-211", "title-28")
        try {
            item.moveTo(RestBreakCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRestBreakCycle("code-211", "title-28")
        item.status = RestBreakCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RestBreakCycleStatus.OPEN) }
    }
}
