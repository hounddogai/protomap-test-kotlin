package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackViolationSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackViolationSlotStore()
        val item = newFallbackViolationSlot("code-228", "title-82")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
