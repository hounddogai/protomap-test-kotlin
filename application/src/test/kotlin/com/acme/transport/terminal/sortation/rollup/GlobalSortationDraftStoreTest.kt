package com.acme.transport.terminal.sortation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalSortationDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalSortationDraftStore()
        val item = newGlobalSortationDraft("code-191", "title-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
