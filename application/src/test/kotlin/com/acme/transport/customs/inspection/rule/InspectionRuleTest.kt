package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionRule("code-675", "label-476")
        assertEquals(InspectionRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newInspectionRule("code-675", "label-476")
        try {
            item.moveTo(InspectionRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionRule("code-675", "label-476")
        item.status = InspectionRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionRuleStatus.REQUESTED) }
    }
}
