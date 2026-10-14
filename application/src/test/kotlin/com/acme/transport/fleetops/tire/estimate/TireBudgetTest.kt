package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newTireBudget("code-593", "title-558")
        assertEquals(TireBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTireBudget("code-593", "title-558")
        try {
            item.moveTo(TireBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTireBudget("code-593", "title-558")
        item.status = TireBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(TireBudgetStatus.CREATED) }
    }
}
