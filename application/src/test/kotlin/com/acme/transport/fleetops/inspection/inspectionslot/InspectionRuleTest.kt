package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionRule("code-535", "label-796")
        assertEquals(InspectionRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionRule("code-535", "label-796")
        try {
            item.moveTo(InspectionRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionRule("code-535", "label-796")
        item.status = InspectionRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionRuleStatus.PROPOSED) }
    }
}
