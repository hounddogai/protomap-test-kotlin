package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCutoffPlanTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalCutoffPlan("code-107", "name-780")
        assertEquals(SeasonalCutoffPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSeasonalCutoffPlan("code-107", "name-780")
        try {
            item.moveTo(SeasonalCutoffPlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCutoffPlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalCutoffPlan("code-107", "name-780")
        item.status = SeasonalCutoffPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalCutoffPlanStatus.OPEN) }
    }
}
