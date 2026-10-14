package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalNearMissMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalNearMissMilestoneStore()
        val item = newRegionalNearMissMilestone("code-950", "name-632")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
