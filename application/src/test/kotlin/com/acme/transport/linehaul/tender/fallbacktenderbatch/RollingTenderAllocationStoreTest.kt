package com.acme.transport.linehaul.tender.fallbacktenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTenderAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTenderAllocationStore()
        val item = newRollingTenderAllocation("code-582", "label-383")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
