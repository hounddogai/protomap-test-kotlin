package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDropRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDropRuleStore()
        val item = newPrimaryDropRule("code-477", "label-518")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
