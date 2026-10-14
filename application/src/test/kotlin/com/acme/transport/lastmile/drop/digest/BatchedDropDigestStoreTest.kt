package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDropDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDropDigestStore()
        val item = newBatchedDropDigest("code-345", "title-812")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
