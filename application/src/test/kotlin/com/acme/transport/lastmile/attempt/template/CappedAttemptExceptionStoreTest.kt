package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAttemptExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAttemptExceptionStore()
        val item = newCappedAttemptException("code-836", "name-870")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
