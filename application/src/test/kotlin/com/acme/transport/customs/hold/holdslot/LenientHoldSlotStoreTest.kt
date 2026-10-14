package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHoldSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHoldSlotStore()
        val item = newLenientHoldSlot("code-657", "name-98")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
