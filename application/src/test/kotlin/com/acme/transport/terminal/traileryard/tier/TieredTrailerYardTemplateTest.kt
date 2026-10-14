package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTrailerYardTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredTrailerYardTemplate("code-13", "label-484")
        assertEquals(TieredTrailerYardTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTieredTrailerYardTemplate("code-13", "label-484")
        try {
            item.moveTo(TieredTrailerYardTemplateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTrailerYardTemplateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredTrailerYardTemplate("code-13", "label-484")
        item.status = TieredTrailerYardTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredTrailerYardTemplateStatus.PLANNED) }
    }
}
