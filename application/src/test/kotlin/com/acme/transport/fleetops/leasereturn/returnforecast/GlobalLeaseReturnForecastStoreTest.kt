package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalLeaseReturnForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalLeaseReturnForecastStore()
        val item = newGlobalLeaseReturnForecast("code-229", "label-2")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
