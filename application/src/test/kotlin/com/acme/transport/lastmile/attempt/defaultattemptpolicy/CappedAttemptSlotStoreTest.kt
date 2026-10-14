package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAttemptSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAttemptSlotStore()
        val item = newCappedAttemptSlot("code-809", "name-905")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
