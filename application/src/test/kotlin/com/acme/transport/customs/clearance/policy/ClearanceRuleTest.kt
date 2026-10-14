package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceRule("code-202", "name-424")
        assertEquals(ClearanceRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newClearanceRule("code-202", "name-424")
        try {
            item.moveTo(ClearanceRuleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceRuleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceRule("code-202", "name-424")
        item.status = ClearanceRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceRuleStatus.PROPOSED) }
    }
}
