package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientSwapGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientSwapGroup("code-73", "label-590")
        assertEquals(LenientSwapGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientSwapGroup("code-73", "label-590")
        try {
            item.moveTo(LenientSwapGroupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientSwapGroupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientSwapGroup("code-73", "label-590")
        item.status = LenientSwapGroupStatus.CLOSED
        assertFails("move") { item.moveTo(LenientSwapGroupStatus.DRAFT) }
    }
}
