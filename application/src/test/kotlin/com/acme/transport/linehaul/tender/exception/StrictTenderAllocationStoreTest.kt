package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTenderAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTenderAllocationStore()
        val item = newStrictTenderAllocation("code-438", "name-287")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
