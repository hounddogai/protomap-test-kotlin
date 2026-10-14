package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationRequestStore()
        val item = newViolationRequest("code-509", "name-302")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
