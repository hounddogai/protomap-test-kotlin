package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerThresholdStore()
        val item = newLockerThreshold("code-991", "name-128")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
