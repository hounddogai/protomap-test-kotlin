package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBackhaulPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBackhaulPolicyStore()
        val item = newRollingBackhaulPolicy("code-111", "title-448")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
