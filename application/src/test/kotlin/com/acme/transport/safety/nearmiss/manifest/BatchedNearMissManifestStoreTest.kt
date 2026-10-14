package com.acme.transport.safety.nearmiss.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNearMissManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNearMissManifestStore()
        val item = newBatchedNearMissManifest("code-514", "title-647")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
