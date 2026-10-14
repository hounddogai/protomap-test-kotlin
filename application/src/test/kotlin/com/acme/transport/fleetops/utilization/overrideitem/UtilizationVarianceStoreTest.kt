package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationVarianceStore()
        val item = newUtilizationVariance("code-776", "name-216")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
