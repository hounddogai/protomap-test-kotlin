package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredHubBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredHubBudget("code-713", "title-732")
        assertEquals(TieredHubBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTieredHubBudget("code-713", "title-732")
        try {
            item.moveTo(TieredHubBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredHubBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredHubBudget("code-713", "title-732")
        item.status = TieredHubBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredHubBudgetStatus.PROPOSED) }
    }
}
