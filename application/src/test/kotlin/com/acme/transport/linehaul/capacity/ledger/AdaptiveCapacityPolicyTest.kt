package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCapacityPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        assertEquals(AdaptiveCapacityPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        try {
            item.moveTo(AdaptiveCapacityPolicyStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCapacityPolicyStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        item.status = AdaptiveCapacityPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveCapacityPolicyStatus.DRAFT) }
    }
}
