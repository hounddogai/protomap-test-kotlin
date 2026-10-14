package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveNodeAllocation("code-797", "name-70")
        assertEquals(AdaptiveNodeAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAdaptiveNodeAllocation("code-797", "name-70")
        try {
            item.moveTo(AdaptiveNodeAllocationStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeAllocationStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveNodeAllocation("code-797", "name-70")
        item.status = AdaptiveNodeAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveNodeAllocationStatus.PROPOSED) }
    }
}
