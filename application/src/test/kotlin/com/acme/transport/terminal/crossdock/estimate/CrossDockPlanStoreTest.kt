package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockPlanStore()
        val item = newCrossDockPlan("code-277", "title-973")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
