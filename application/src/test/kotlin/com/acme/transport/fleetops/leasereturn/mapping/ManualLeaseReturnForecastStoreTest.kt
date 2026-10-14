package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualLeaseReturnForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualLeaseReturnForecastStore()
        val item = newManualLeaseReturnForecast("code-467", "label-792")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
