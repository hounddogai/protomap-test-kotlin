package com.acme.transport.crewing.swap.swapnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapProfileTest {
    @Test
    fun `starts in new`() {
        val item = newSwapProfile("code-859", "name-526")
        assertEquals(SwapProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newSwapProfile("code-859", "name-526")
        try {
            item.moveTo(SwapProfileStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapProfileStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSwapProfile("code-859", "name-526")
        item.status = SwapProfileStatus.FAILED
        assertFails("move") { item.moveTo(SwapProfileStatus.NEW) }
    }
}
