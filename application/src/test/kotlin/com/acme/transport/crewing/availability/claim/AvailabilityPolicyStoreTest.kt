package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityPolicyStore()
        val item = newAvailabilityPolicy("code-248", "title-644")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
