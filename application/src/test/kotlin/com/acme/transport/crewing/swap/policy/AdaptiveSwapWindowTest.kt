package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveSwapWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        assertEquals(AdaptiveSwapWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        try {
            item.moveTo(AdaptiveSwapWindowStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveSwapWindowStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        item.status = AdaptiveSwapWindowStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveSwapWindowStatus.IDLE) }
    }
}
