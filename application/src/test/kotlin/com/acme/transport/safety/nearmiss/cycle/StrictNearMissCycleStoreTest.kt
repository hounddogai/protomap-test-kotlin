package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictNearMissCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictNearMissCycleStore()
        val item = newStrictNearMissCycle("code-579", "title-470")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
