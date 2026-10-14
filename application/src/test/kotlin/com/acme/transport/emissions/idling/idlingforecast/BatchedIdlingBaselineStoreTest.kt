package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedIdlingBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedIdlingBaselineStore()
        val item = newBatchedIdlingBaseline("code-105", "title-111")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
