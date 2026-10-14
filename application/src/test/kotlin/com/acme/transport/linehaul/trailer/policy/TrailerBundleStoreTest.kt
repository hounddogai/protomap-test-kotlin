package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerBundleStore()
        val item = newTrailerBundle("code-769", "name-397")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
