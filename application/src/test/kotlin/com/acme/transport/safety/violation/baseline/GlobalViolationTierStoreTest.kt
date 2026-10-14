package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalViolationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalViolationTierStore()
        val item = newGlobalViolationTier("code-45", "name-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
