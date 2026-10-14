package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalHoldPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalHoldPolicy("code-562", "name-767")
        assertEquals(GlobalHoldPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalHoldPolicy("code-562", "name-767")
        try {
            item.moveTo(GlobalHoldPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalHoldPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalHoldPolicy("code-562", "name-767")
        item.status = GlobalHoldPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalHoldPolicyStatus.PROPOSED) }
    }
}
