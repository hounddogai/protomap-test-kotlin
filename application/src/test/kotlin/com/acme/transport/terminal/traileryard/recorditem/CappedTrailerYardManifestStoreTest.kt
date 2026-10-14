package com.acme.transport.terminal.traileryard.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTrailerYardManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTrailerYardManifestStore()
        val item = newCappedTrailerYardManifest("code-85", "name-996")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
