package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardProfileTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerYardProfile("code-410", "label-572")
        assertEquals(TrailerYardProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTrailerYardProfile("code-410", "label-572")
        try {
            item.moveTo(TrailerYardProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerYardProfile("code-410", "label-572")
        item.status = TrailerYardProfileStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerYardProfileStatus.CREATED) }
    }
}
