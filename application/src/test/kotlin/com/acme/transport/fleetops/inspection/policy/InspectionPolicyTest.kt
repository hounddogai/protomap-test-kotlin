package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionPolicy("code-945", "label-606")
        assertEquals(InspectionPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newInspectionPolicy("code-945", "label-606")
        try {
            item.moveTo(InspectionPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionPolicy("code-945", "label-606")
        item.status = InspectionPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionPolicyStatus.IDLE) }
    }
}
