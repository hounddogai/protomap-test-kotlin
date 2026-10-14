package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissAllocationStore()
        val item = newNearMissAllocation("code-428", "name-155")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
