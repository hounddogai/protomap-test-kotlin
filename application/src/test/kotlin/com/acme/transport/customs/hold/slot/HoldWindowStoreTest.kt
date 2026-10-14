package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldWindowStore()
        val item = newHoldWindow("code-273", "name-625")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
