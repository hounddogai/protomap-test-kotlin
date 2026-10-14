package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAccessorialProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAccessorialProfileStore()
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
