package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissAdjustmentStore()
        val item = newNearMissAdjustment("code-100", "title-557")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
