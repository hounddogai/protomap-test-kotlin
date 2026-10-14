package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapDraftTest {
    @Test
    fun `starts in created`() {
        val item = newSwapDraft("code-269", "name-757")
        assertEquals(SwapDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSwapDraft("code-269", "name-757")
        try {
            item.moveTo(SwapDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSwapDraft("code-269", "name-757")
        item.status = SwapDraftStatus.RETIRED
        assertFails("move") { item.moveTo(SwapDraftStatus.CREATED) }
    }
}
