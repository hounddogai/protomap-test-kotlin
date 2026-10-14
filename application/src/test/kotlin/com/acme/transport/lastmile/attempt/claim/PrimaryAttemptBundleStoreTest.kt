package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAttemptBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAttemptBundleStore()
        val item = newPrimaryAttemptBundle("code-202", "title-258")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
