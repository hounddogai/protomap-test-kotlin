package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyEvChargeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyEvChargeMappingStore()
        val item = newLegacyEvChargeMapping("code-777", "name-851")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
