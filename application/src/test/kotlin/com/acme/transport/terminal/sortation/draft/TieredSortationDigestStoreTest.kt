package com.acme.transport.terminal.sortation.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredSortationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredSortationDigestStore()
        val item = newTieredSortationDigest("code-135", "title-661")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
