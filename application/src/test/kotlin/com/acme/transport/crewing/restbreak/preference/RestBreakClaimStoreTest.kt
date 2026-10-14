package com.acme.transport.crewing.restbreak.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakClaimStore()
        val item = newRestBreakClaim("code-373", "label-425")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
