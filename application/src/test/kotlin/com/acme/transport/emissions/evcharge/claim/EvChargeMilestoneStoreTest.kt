package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeMilestoneStore()
        val item = newEvChargeMilestone("code-22", "name-737")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
