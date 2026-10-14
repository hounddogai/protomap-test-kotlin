package com.acme.transport.lastmile.locker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalLockerBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalLockerBatchStore()
        val item = newGlobalLockerBatch("code-703", "name-117")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
