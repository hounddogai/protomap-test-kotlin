package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationTargetStore()
        val item = newUtilizationTarget("code-854", "title-250")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
