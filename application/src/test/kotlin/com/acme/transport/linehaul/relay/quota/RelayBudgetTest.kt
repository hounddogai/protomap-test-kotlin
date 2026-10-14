package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newRelayBudget("code-809", "label-310")
        assertEquals(RelayBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRelayBudget("code-809", "label-310")
        try {
            item.moveTo(RelayBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRelayBudget("code-809", "label-310")
        item.status = RelayBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(RelayBudgetStatus.CREATED) }
    }
}
