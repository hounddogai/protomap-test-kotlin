package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldEstimateStore()
        val item = newHoldEstimate("code-547", "name-507")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
