package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationQuotaStore()
        val item = newUtilizationQuota("code-697", "label-279")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
