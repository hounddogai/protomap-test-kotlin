package com.acme.transport.terminal.sortation.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictSortationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictSortationManifestStore()
        val item = newStrictSortationManifest("code-552", "label-376")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
