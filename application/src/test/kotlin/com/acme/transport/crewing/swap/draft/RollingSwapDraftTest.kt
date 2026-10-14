package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapDraftTest {
    @Test
    fun `starts in created`() {
        val item = newRollingSwapDraft("code-406", "name-933")
        assertEquals(RollingSwapDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingSwapDraft("code-406", "name-933")
        try {
            item.moveTo(RollingSwapDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingSwapDraft("code-406", "name-933")
        item.status = RollingSwapDraftStatus.RETIRED
        assertFails("move") { item.moveTo(RollingSwapDraftStatus.CREATED) }
    }
}
