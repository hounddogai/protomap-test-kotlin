package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffRuleStore()
        val item = newCutoffRule("code-744", "title-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
