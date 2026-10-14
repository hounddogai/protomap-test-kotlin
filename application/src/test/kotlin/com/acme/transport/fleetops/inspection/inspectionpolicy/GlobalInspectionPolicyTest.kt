package com.acme.transport.fleetops.inspection.inspectionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalInspectionPolicy("code-22", "name-510")
        assertEquals(GlobalInspectionPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalInspectionPolicy("code-22", "name-510")
        try {
            item.moveTo(GlobalInspectionPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalInspectionPolicy("code-22", "name-510")
        item.status = GlobalInspectionPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalInspectionPolicyStatus.PROPOSED) }
    }
}
