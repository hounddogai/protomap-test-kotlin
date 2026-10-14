package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationTierStore()
        val item = newUtilizationTier("code-861", "label-569")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
