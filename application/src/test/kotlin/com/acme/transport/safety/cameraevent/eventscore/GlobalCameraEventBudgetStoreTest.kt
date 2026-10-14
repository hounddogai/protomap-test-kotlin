package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCameraEventBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCameraEventBudgetStore()
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
