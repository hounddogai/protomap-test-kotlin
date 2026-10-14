package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAttemptRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAttemptRecordStore()
        val item = newStrictAttemptRecord("code-679", "name-234")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
