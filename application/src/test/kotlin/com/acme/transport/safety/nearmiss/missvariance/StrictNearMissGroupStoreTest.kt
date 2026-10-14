package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictNearMissGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictNearMissGroupStore()
        val item = newStrictNearMissGroup("code-386", "name-417")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
