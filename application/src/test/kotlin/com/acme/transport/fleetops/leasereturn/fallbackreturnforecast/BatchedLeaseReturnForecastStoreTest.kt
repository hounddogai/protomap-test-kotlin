package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLeaseReturnForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLeaseReturnForecastStore()
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
