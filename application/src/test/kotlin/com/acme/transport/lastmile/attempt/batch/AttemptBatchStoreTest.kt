package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptBatchStore()
        val item = newAttemptBatch("code-751", "name-215")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
