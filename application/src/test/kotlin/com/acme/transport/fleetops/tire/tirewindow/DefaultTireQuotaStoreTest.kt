package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTireQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTireQuotaStore()
        val item = newDefaultTireQuota("code-465", "label-692")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
