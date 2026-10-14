package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapWindowTest {
    @Test
    fun `starts in created`() {
        val item = newSwapWindow("code-882", "label-567")
        assertEquals(SwapWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSwapWindow("code-882", "label-567")
        try {
            item.moveTo(SwapWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSwapWindow("code-882", "label-567")
        item.status = SwapWindowStatus.RETIRED
        assertFails("move") { item.moveTo(SwapWindowStatus.CREATED) }
    }
}
