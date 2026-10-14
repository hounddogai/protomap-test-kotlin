package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDefectSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDefectSlotStore()
        val item = newStrictDefectSlot("code-314", "name-575")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
