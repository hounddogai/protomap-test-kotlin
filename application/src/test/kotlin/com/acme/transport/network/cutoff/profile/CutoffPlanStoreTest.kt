package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffPlanStore()
        val item = newCutoffPlan("code-903", "title-177")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
