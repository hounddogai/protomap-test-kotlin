package com.acme.transport.customs.hold.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldPolicyStore()
        val item = newHoldPolicy("code-98", "label-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
