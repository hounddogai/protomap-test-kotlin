package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingViolationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingViolationWindowStore()
        val item = newRollingViolationWindow("code-204", "name-82")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
