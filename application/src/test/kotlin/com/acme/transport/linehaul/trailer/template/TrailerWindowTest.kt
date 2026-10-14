package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerWindow("code-847", "title-990")
        assertEquals(TrailerWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTrailerWindow("code-847", "title-990")
        try {
            item.moveTo(TrailerWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerWindow("code-847", "title-990")
        item.status = TrailerWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerWindowStatus.PROPOSED) }
    }
}
