package com.acme.transport.fleetops.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackInspectionPolicy("code-3", "name-713")
        assertEquals(FallbackInspectionPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackInspectionPolicy("code-3", "name-713")
        try {
            item.moveTo(FallbackInspectionPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackInspectionPolicy("code-3", "name-713")
        item.status = FallbackInspectionPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackInspectionPolicyStatus.CREATED) }
    }
}
