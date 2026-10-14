package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictNodeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictNodeBundleStore()
        val item = newStrictNodeBundle("code-30", "label-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
