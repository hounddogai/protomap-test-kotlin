package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingBatchStore()
        val item = newIdlingBatch("code-820", "title-318")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
