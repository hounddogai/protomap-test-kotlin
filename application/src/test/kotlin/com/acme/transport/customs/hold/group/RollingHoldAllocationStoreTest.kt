package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingHoldAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingHoldAllocationStore()
        val item = newRollingHoldAllocation("code-420", "title-896")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
