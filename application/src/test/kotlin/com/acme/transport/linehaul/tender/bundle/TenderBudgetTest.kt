package com.acme.transport.linehaul.tender.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newTenderBudget("code-325", "label-428")
        assertEquals(TenderBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTenderBudget("code-325", "label-428")
        try {
            item.moveTo(TenderBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTenderBudget("code-325", "label-428")
        item.status = TenderBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(TenderBudgetStatus.OPEN) }
    }
}
