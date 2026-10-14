package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubBatchStore()
        val item = newHubBatch("code-160", "name-559")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
