package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTenderTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newCappedTenderTemplate("code-12", "label-638")
        assertEquals(CappedTenderTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCappedTenderTemplate("code-12", "label-638")
        try {
            item.moveTo(CappedTenderTemplateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTenderTemplateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedTenderTemplate("code-12", "label-638")
        item.status = CappedTenderTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedTenderTemplateStatus.OPEN) }
    }
}
