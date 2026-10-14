package com.acme.transport.lastmile.attempt.attemptdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAttemptDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAttemptDigestStore()
        val item = newWeightedAttemptDigest("code-205", "title-390")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
