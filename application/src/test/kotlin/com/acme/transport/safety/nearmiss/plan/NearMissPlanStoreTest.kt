package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissPlanStore()
        val item = newNearMissPlan("code-764", "label-89")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
