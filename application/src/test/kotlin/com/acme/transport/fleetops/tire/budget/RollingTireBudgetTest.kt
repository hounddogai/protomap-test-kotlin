package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTireBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTireBudget("code-737", "title-635")
        assertEquals(RollingTireBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingTireBudget("code-737", "title-635")
        try {
            item.moveTo(RollingTireBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTireBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTireBudget("code-737", "title-635")
        item.status = RollingTireBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTireBudgetStatus.CREATED) }
    }
}
