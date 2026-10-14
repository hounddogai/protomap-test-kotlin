package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalSwapPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalSwapPlanStore()
        val item = newGlobalSwapPlan("code-237", "name-837")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
