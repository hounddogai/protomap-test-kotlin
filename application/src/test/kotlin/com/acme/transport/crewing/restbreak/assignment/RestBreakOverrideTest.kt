package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newRestBreakOverride("code-631", "name-125")
        assertEquals(RestBreakOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRestBreakOverride("code-631", "name-125")
        try {
            item.moveTo(RestBreakOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRestBreakOverride("code-631", "name-125")
        item.status = RestBreakOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(RestBreakOverrideStatus.DRAFT) }
    }
}
