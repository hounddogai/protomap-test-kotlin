package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultLockerSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultLockerSlotStore()
        val item = newDefaultLockerSlot("code-362", "name-365")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
