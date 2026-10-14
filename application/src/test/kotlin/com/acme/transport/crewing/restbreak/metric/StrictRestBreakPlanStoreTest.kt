package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRestBreakPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRestBreakPlanStore()
        val item = newStrictRestBreakPlan("code-512", "title-1")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
