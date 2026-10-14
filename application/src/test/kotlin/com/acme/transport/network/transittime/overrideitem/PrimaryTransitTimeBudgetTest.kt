package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryTransitTimeBudget("code-528", "name-151")
        assertEquals(PrimaryTransitTimeBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryTransitTimeBudget("code-528", "name-151")
        try {
            item.moveTo(PrimaryTransitTimeBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryTransitTimeBudget("code-528", "name-151")
        item.status = PrimaryTransitTimeBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryTransitTimeBudgetStatus.CREATED) }
    }
}
