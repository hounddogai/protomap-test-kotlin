package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationEntryStore()
        val item = newUtilizationEntry("code-730", "title-875")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
