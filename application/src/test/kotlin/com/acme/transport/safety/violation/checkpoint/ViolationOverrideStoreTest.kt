package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationOverrideStore()
        val item = newViolationOverride("code-858", "name-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
