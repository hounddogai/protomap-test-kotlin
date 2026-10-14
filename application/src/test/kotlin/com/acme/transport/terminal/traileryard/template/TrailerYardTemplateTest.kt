package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerYardTemplate("code-631", "title-589")
        assertEquals(TrailerYardTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTrailerYardTemplate("code-631", "title-589")
        try {
            item.moveTo(TrailerYardTemplateStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardTemplateStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerYardTemplate("code-631", "title-589")
        item.status = TrailerYardTemplateStatus.FAILED
        assertFails("move") { item.moveTo(TrailerYardTemplateStatus.NEW) }
    }
}
