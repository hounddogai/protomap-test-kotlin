package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissBatchStore()
        val item = newNearMissBatch("code-713", "title-68")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
