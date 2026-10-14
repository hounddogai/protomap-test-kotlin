package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAttemptProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAttemptProfileStore()
        val item = newWeightedAttemptProfile("code-488", "title-737")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
