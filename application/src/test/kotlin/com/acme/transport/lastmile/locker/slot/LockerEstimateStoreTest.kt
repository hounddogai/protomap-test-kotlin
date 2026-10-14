package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerEstimateStore()
        val item = newLockerEstimate("code-40", "label-515")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
