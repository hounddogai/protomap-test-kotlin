package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptEstimateStore()
        val item = newAttemptEstimate("code-292", "name-551")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
