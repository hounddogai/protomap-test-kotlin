package com.acme.transport.safety.violation.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationRuleTest {
    @Test
    fun `starts in created`() {
        val item = newViolationRule("code-432", "label-372")
        assertEquals(ViolationRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newViolationRule("code-432", "label-372")
        try {
            item.moveTo(ViolationRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newViolationRule("code-432", "label-372")
        item.status = ViolationRuleStatus.RETIRED
        assertFails("move") { item.moveTo(ViolationRuleStatus.CREATED) }
    }
}
