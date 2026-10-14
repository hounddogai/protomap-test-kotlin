package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellPolicyStore()
        val item = newDwellPolicy("code-858", "title-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
