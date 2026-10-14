package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerTemplate("code-110", "title-971")
        assertEquals(TrailerTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTrailerTemplate("code-110", "title-971")
        try {
            item.moveTo(TrailerTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerTemplate("code-110", "title-971")
        item.status = TrailerTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerTemplateStatus.DRAFT) }
    }
}
