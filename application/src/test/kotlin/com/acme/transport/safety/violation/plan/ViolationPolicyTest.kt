package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newViolationPolicy("code-504", "title-152")
        assertEquals(ViolationPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newViolationPolicy("code-504", "title-152")
        try {
            item.moveTo(ViolationPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newViolationPolicy("code-504", "title-152")
        item.status = ViolationPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ViolationPolicyStatus.PROPOSED) }
    }
}
