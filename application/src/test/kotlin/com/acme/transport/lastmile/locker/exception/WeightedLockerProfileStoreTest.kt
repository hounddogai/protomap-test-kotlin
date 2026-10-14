package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLockerProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLockerProfileStore()
        val item = newWeightedLockerProfile("code-891", "title-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
