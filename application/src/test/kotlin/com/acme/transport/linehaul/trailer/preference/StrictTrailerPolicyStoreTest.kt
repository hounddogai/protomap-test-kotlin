package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTrailerPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTrailerPolicyStore()
        val item = newStrictTrailerPolicy("code-517", "title-484")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
