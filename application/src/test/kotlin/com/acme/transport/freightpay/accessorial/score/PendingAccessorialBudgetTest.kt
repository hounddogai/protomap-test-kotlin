package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAccessorialBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newPendingAccessorialBudget("code-516", "name-96")
        assertEquals(PendingAccessorialBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingAccessorialBudget("code-516", "name-96")
        try {
            item.moveTo(PendingAccessorialBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAccessorialBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingAccessorialBudget("code-516", "name-96")
        item.status = PendingAccessorialBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(PendingAccessorialBudgetStatus.CREATED) }
    }
}
