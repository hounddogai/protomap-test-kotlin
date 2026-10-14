package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionPolicy("code-880", "title-131")
        assertEquals(InspectionPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newInspectionPolicy("code-880", "title-131")
        try {
            item.moveTo(InspectionPolicyStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionPolicyStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionPolicy("code-880", "title-131")
        item.status = InspectionPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionPolicyStatus.PLANNED) }
    }
}
