package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTrailerManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTrailerManifestStore()
        val item = newStrictTrailerManifest("code-524", "title-351")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
