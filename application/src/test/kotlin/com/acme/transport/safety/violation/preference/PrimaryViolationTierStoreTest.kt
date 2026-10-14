package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryViolationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryViolationTierStore()
        val item = newPrimaryViolationTier("code-779", "label-715")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
