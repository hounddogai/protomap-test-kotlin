package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptRecordStore()
        val item = newAttemptRecord("code-273", "name-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
