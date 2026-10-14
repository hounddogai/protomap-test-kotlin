package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTrailerYardRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTrailerYardRuleStore()
        val item = newRollingTrailerYardRule("code-583", "title-870")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
