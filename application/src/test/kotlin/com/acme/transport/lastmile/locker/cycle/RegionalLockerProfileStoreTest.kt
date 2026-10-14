package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalLockerProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalLockerProfileStore()
        val item = newRegionalLockerProfile("code-866", "name-380")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
