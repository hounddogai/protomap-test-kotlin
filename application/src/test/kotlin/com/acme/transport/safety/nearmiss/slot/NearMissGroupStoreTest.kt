package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissGroupStore()
        val item = newNearMissGroup("code-348", "title-658")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
