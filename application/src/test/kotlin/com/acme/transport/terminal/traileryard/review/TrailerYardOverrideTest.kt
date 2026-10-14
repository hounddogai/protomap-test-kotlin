package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerYardOverride("code-754", "name-948")
        assertEquals(TrailerYardOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTrailerYardOverride("code-754", "name-948")
        try {
            item.moveTo(TrailerYardOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerYardOverride("code-754", "name-948")
        item.status = TrailerYardOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerYardOverrideStatus.REQUESTED) }
    }
}
