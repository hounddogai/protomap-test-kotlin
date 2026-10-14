package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyInspectionRule("code-908", "name-981")
        assertEquals(LegacyInspectionRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLegacyInspectionRule("code-908", "name-981")
        try {
            item.moveTo(LegacyInspectionRuleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionRuleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyInspectionRule("code-908", "name-981")
        item.status = LegacyInspectionRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyInspectionRuleStatus.REQUESTED) }
    }
}
