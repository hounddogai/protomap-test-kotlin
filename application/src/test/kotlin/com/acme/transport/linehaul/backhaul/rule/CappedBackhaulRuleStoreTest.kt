package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBackhaulRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBackhaulRuleStore()
        val item = newCappedBackhaulRule("code-556", "label-208")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
