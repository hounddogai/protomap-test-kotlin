package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSwapPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSwapPlanStore()
        val item = newRollingSwapPlan("code-981", "name-594")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
