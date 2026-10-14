package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAttemptBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAttemptBaselineStore()
        val item = newManualAttemptBaseline("code-895", "title-900")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
