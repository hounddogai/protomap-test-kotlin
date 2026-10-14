package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptSnapshotStore()
        val item = newAttemptSnapshot("code-219", "title-131")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
