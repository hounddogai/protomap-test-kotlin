package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldTargetStore()
        val item = newHoldTarget("code-530", "name-566")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
