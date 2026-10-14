package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockRuleStore()
        val item = newCrossDockRule("code-793", "name-475")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
