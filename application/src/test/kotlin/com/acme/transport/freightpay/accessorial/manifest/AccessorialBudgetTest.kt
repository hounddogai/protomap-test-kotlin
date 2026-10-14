package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialBudget("code-809", "title-633")
        assertEquals(AccessorialBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAccessorialBudget("code-809", "title-633")
        try {
            item.moveTo(AccessorialBudgetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialBudgetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialBudget("code-809", "title-633")
        item.status = AccessorialBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialBudgetStatus.REQUESTED) }
    }
}
