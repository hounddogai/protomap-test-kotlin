package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTrailerYardExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newTieredTrailerYardException("code-715", "label-390")
        assertEquals(TieredTrailerYardExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTieredTrailerYardException("code-715", "label-390")
        try {
            item.moveTo(TieredTrailerYardExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTrailerYardExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredTrailerYardException("code-715", "label-390")
        item.status = TieredTrailerYardExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(TieredTrailerYardExceptionStatus.CREATED) }
    }
}
