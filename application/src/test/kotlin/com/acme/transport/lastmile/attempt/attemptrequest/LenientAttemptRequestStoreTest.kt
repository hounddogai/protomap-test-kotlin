package com.acme.transport.lastmile.attempt.attemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAttemptRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAttemptRequestStore()
        val item = newLenientAttemptRequest("code-141", "name-247")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
