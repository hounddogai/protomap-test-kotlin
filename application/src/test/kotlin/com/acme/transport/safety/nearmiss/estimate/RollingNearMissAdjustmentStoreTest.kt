package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingNearMissAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingNearMissAdjustmentStore()
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
