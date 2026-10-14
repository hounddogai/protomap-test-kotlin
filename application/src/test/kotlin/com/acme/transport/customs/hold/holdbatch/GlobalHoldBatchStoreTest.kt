package com.acme.transport.customs.hold.holdbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalHoldBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalHoldBatchStore()
        val item = newGlobalHoldBatch("code-688", "label-960")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
