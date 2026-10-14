package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeRuleStore()
        val item = newEvChargeRule("code-570", "name-556")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
