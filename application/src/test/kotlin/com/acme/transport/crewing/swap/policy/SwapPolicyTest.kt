package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newSwapPolicy("code-484", "label-966")
        assertEquals(SwapPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSwapPolicy("code-484", "label-966")
        try {
            item.moveTo(SwapPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSwapPolicy("code-484", "label-966")
        item.status = SwapPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(SwapPolicyStatus.CREATED) }
    }
}
