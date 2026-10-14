package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySortationTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimarySortationTemplate("code-599", "title-620")
        assertEquals(PrimarySortationTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimarySortationTemplate("code-599", "title-620")
        try {
            item.moveTo(PrimarySortationTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySortationTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimarySortationTemplate("code-599", "title-620")
        item.status = PrimarySortationTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(PrimarySortationTemplateStatus.DRAFT) }
    }
}
