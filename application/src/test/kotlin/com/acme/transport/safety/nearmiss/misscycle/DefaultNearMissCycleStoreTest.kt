package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultNearMissCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultNearMissCycleStore()
        val item = newDefaultNearMissCycle("code-63", "name-142")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
