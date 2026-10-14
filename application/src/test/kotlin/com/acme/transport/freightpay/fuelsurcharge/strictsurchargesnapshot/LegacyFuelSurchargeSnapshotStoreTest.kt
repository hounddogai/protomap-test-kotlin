package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyFuelSurchargeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyFuelSurchargeSnapshotStore()
        val item = newLegacyFuelSurchargeSnapshot("code-891", "title-271")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
