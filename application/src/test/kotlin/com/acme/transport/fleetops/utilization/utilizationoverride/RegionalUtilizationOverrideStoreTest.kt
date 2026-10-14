package com.acme.transport.fleetops.utilization.utilizationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalUtilizationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalUtilizationOverrideStore()
        val item = newRegionalUtilizationOverride("code-996", "name-797")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
