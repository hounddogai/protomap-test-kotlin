package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropManifestStore()
        val item = newDropManifest("code-919", "label-480")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
