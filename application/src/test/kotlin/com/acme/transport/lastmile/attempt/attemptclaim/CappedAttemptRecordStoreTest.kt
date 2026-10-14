package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAttemptRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAttemptRecordStore()
        val item = newCappedAttemptRecord("code-728", "name-231")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
