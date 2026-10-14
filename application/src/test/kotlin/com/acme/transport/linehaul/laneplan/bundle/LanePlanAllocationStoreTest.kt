package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanAllocationStore()
        val item = newLanePlanAllocation("code-893", "title-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
