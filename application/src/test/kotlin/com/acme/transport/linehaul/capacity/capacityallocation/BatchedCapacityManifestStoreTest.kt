package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityManifestStore()
        val item = newBatchedCapacityManifest("code-980", "name-192")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
