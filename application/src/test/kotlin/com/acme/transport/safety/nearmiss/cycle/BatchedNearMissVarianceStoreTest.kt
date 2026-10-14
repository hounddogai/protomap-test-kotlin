package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNearMissVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNearMissVarianceStore()
        val item = newBatchedNearMissVariance("code-443", "title-808")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
