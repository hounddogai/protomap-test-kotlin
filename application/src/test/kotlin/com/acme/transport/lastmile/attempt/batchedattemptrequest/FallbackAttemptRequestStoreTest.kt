package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAttemptRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAttemptRequestStore()
        val item = newFallbackAttemptRequest("code-934", "label-85")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
