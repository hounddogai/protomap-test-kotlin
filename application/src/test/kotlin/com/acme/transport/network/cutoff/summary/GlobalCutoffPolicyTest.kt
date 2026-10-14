package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCutoffPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        assertEquals(GlobalCutoffPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        try {
            item.moveTo(GlobalCutoffPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCutoffPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        item.status = GlobalCutoffPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalCutoffPolicyStatus.PROPOSED) }
    }
}
