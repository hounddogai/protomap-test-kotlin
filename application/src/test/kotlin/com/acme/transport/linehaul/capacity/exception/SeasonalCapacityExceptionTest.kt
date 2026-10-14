package com.acme.transport.linehaul.capacity.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCapacityExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalCapacityException("code-477", "name-913")
        assertEquals(SeasonalCapacityExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSeasonalCapacityException("code-477", "name-913")
        try {
            item.moveTo(SeasonalCapacityExceptionStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCapacityExceptionStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalCapacityException("code-477", "name-913")
        item.status = SeasonalCapacityExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalCapacityExceptionStatus.DRAFT) }
    }
}
