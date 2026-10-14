package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingSwapRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingSwapRecordStore()
        val item = newPendingSwapRecord("code-762", "label-436")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
