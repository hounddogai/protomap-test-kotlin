package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterMappingTest {
    @Test
    fun `starts in open`() {
        val item = newStrictRosterMapping("code-77", "label-594")
        assertEquals(StrictRosterMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictRosterMapping("code-77", "label-594")
        try {
            item.moveTo(StrictRosterMappingStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterMappingStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictRosterMapping("code-77", "label-594")
        item.status = StrictRosterMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictRosterMappingStatus.OPEN) }
    }
}
