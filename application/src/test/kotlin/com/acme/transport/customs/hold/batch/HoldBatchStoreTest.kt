package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldBatchStore()
        val item = newHoldBatch("code-102", "title-672")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
