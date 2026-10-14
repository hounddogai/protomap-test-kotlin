package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAuditBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredAuditBudget("code-953", "title-264")
        assertEquals(TieredAuditBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredAuditBudget("code-953", "title-264")
        try {
            item.moveTo(TieredAuditBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAuditBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredAuditBudget("code-953", "title-264")
        item.status = TieredAuditBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredAuditBudgetStatus.REQUESTED) }
    }
}
