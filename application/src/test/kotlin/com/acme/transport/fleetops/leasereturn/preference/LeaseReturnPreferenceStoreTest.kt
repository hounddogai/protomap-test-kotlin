package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnPreferenceStore()
        val item = newLeaseReturnPreference("code-937", "name-524")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
