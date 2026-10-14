package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationClaimStore()
        val item = newViolationClaim("code-166", "title-500")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
