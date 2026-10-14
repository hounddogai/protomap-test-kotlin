package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacySortationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacySortationManifestStore()
        val item = newLegacySortationManifest("code-51", "name-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
