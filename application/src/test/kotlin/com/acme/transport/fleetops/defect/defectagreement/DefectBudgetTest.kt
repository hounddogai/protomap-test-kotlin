package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectBudget("code-360", "label-649")
        assertEquals(DefectBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefectBudget("code-360", "label-649")
        try {
            item.moveTo(DefectBudgetStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectBudgetStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectBudget("code-360", "label-649")
        item.status = DefectBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectBudgetStatus.REQUESTED) }
    }
}
