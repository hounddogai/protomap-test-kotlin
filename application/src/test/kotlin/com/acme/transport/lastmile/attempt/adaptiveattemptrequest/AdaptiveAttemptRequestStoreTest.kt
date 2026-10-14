package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAttemptRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAttemptRequestStore()
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
