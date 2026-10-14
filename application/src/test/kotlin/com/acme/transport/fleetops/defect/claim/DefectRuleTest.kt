package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newDefectRule("code-921", "label-821")
        assertEquals(DefectRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefectRule("code-921", "label-821")
        try {
            item.moveTo(DefectRuleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectRuleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefectRule("code-921", "label-821")
        item.status = DefectRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(DefectRuleStatus.PLANNED) }
    }
}
