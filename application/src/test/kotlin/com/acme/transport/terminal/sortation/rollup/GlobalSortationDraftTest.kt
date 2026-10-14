package com.acme.transport.terminal.sortation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSortationDraftTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalSortationDraft("code-191", "title-803")
        assertEquals(GlobalSortationDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalSortationDraft("code-191", "title-803")
        try {
            item.moveTo(GlobalSortationDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSortationDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalSortationDraft("code-191", "title-803")
        item.status = GlobalSortationDraftStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalSortationDraftStatus.CREATED) }
    }
}
