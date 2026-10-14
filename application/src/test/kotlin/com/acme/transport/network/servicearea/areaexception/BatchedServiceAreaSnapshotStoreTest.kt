package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedServiceAreaSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedServiceAreaSnapshotStore()
        val item = newBatchedServiceAreaSnapshot("code-34", "name-504")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
