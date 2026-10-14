package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNearMissMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNearMissMappingStore()
        val item = newBatchedNearMissMapping("code-384", "name-350")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
