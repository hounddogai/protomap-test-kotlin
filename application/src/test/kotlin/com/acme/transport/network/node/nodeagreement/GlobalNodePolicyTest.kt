package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalNodePolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalNodePolicy("code-493", "label-20")
        assertEquals(GlobalNodePolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalNodePolicy("code-493", "label-20")
        try {
            item.moveTo(GlobalNodePolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalNodePolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalNodePolicy("code-493", "label-20")
        item.status = GlobalNodePolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalNodePolicyStatus.PROPOSED) }
    }
}
