package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationManifestStore()
        val item = newSortationManifest("code-371", "label-219")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
