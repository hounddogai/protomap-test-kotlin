package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerPolicyStore()
        val item = newTrailerPolicy("code-729", "label-134")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
