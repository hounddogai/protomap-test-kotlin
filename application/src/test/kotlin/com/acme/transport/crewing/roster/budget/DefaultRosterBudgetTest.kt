package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRosterBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultRosterBudget("code-240", "title-578")
        assertEquals(DefaultRosterBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultRosterBudget("code-240", "title-578")
        try {
            item.moveTo(DefaultRosterBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRosterBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultRosterBudget("code-240", "title-578")
        item.status = DefaultRosterBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultRosterBudgetStatus.CREATED) }
    }
}
