package com.acme.transport.terminal.sortation.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationTemplate("code-101", "label-69")
        assertEquals(SortationTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSortationTemplate("code-101", "label-69")
        try {
            item.moveTo(SortationTemplateStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationTemplateStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationTemplate("code-101", "label-69")
        item.status = SortationTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(SortationTemplateStatus.DRAFT) }
    }
}
