package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEvChargeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEvChargeBatchStore()
        val item = newPendingEvChargeBatch("code-479", "label-319")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
