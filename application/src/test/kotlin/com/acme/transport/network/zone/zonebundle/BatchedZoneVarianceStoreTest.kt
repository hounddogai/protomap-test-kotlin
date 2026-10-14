package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedZoneVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedZoneVarianceStore()
        val item = newBatchedZoneVariance("code-500", "name-844")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
