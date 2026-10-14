package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptOverrideStore()
        val item = newAttemptOverride("code-999", "title-259")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
