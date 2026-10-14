package com.acme.transport.customs.hold.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldPolicy("code-98", "label-948")
        assertEquals(HoldPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newHoldPolicy("code-98", "label-948")
        try {
            item.moveTo(HoldPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldPolicy("code-98", "label-948")
        item.status = HoldPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(HoldPolicyStatus.IDLE) }
    }
}
