package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDwellRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDwellRuleStore()
        val item = newFallbackDwellRule("code-896", "title-973")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
