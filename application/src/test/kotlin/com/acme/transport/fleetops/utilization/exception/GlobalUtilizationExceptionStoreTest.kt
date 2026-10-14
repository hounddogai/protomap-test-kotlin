package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalUtilizationExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalUtilizationExceptionStore()
        val item = newGlobalUtilizationException("code-487", "name-386")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
