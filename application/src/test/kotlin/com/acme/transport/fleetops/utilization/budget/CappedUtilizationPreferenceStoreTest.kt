package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedUtilizationPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedUtilizationPreferenceStore()
        val item = newCappedUtilizationPreference("code-257", "name-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
