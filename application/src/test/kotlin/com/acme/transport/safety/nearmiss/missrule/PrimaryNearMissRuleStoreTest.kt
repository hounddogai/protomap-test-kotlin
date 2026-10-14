package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryNearMissRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryNearMissRuleStore()
        val item = newPrimaryNearMissRule("code-931", "title-602")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
