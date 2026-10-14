package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalViolationPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalViolationPolicy("code-256", "label-236")
        assertEquals(RegionalViolationPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalViolationPolicy("code-256", "label-236")
        try {
            item.moveTo(RegionalViolationPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalViolationPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalViolationPolicy("code-256", "label-236")
        item.status = RegionalViolationPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalViolationPolicyStatus.CREATED) }
    }
}
