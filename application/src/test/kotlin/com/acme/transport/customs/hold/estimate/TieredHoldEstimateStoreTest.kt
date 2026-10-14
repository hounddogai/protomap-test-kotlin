package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredHoldEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredHoldEstimateStore()
        val item = newTieredHoldEstimate("code-499", "name-70")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
