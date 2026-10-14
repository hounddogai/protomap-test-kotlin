package com.acme.transport.lastmile.attempt.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptBaselineStore()
        val item = newAttemptBaseline("code-551", "title-262")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
