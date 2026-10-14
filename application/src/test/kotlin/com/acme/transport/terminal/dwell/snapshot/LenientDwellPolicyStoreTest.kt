package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDwellPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDwellPolicyStore()
        val item = newLenientDwellPolicy("code-938", "name-767")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
