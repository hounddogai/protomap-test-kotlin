package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualViolationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualViolationBatchStore()
        val item = newManualViolationBatch("code-498", "label-620")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
