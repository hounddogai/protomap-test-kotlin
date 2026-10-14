package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissCycleStore()
        val item = newNearMissCycle("code-461", "label-415")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
