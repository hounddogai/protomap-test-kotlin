package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAttemptClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAttemptClaimStore()
        val item = newDefaultAttemptClaim("code-252", "title-922")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
