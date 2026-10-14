package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalNearMissRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalNearMissRecordStore()
        val item = newGlobalNearMissRecord("code-877", "name-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
