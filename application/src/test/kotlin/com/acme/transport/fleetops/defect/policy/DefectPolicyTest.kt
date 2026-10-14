package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectPolicy("code-424", "title-366")
        assertEquals(DefectPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefectPolicy("code-424", "title-366")
        try {
            item.moveTo(DefectPolicyStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectPolicyStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectPolicy("code-424", "title-366")
        item.status = DefectPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectPolicyStatus.REQUESTED) }
    }
}
