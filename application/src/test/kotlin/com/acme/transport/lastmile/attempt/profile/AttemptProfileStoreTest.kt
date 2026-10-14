package com.acme.transport.lastmile.attempt.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptProfileStore()
        val item = newAttemptProfile("code-726", "name-83")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
