package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualNodePolicyTest {
    @Test
    fun `starts in created`() {
        val item = newManualNodePolicy("code-261", "title-829")
        assertEquals(ManualNodePolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualNodePolicy("code-261", "title-829")
        try {
            item.moveTo(ManualNodePolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualNodePolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualNodePolicy("code-261", "title-829")
        item.status = ManualNodePolicyStatus.RETIRED
        assertFails("move") { item.moveTo(ManualNodePolicyStatus.CREATED) }
    }
}
