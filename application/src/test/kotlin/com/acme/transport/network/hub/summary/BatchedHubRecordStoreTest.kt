package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedHubRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedHubRecordStore()
        val item = newBatchedHubRecord("code-107", "title-434")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
