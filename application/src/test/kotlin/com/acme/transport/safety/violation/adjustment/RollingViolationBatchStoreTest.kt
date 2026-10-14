package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingViolationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingViolationBatchStore()
        val item = newRollingViolationBatch("code-767", "label-260")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
