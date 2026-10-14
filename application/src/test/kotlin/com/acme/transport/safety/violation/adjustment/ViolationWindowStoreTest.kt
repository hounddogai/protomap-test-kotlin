package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationWindowStore()
        val item = newViolationWindow("code-90", "name-792")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
