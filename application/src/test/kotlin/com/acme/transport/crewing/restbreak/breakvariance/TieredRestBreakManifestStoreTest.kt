package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRestBreakManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRestBreakManifestStore()
        val item = newTieredRestBreakManifest("code-43", "name-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
