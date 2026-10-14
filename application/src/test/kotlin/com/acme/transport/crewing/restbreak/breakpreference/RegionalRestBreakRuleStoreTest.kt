package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRestBreakRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRestBreakRuleStore()
        val item = newRegionalRestBreakRule("code-587", "label-103")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
