package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptAdjustmentStore()
        val item = newAttemptAdjustment("code-641", "label-714")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
