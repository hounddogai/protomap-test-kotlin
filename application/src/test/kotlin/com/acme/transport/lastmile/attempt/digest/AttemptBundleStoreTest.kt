package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptBundleStore()
        val item = newAttemptBundle("code-72", "name-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
