package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredSortationRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredSortationRuleStore()
        val item = newTieredSortationRule("code-963", "title-936")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
