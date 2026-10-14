package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalViolationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalViolationBatchStore()
        val item = newGlobalViolationBatch("code-220", "name-396")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
