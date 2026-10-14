package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictLockerThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictLockerThresholdStore()
        val item = newStrictLockerThreshold("code-178", "name-823")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
