package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRosterPlanTest {
    @Test
    fun `starts in created`() {
        val item = newManualRosterPlan("code-72", "title-276")
        assertEquals(ManualRosterPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualRosterPlan("code-72", "title-276")
        try {
            item.moveTo(ManualRosterPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRosterPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualRosterPlan("code-72", "title-276")
        item.status = ManualRosterPlanStatus.RETIRED
        assertFails("move") { item.moveTo(ManualRosterPlanStatus.CREATED) }
    }
}
