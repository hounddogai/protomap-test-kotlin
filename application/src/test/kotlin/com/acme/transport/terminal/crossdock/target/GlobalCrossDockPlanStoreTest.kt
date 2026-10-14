package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCrossDockPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCrossDockPlanStore()
        val item = newGlobalCrossDockPlan("code-704", "label-534")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
