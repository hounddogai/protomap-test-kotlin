package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAttemptThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAttemptThresholdStore()
        val item = newStrictAttemptThreshold("code-848", "label-737")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
