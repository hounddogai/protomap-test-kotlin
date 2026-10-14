package com.acme.transport.crewing.restbreak.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newRestBreakMapping("code-499", "name-168")
        assertEquals(RestBreakMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRestBreakMapping("code-499", "name-168")
        try {
            item.moveTo(RestBreakMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRestBreakMapping("code-499", "name-168")
        item.status = RestBreakMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RestBreakMappingStatus.PROPOSED) }
    }
}
