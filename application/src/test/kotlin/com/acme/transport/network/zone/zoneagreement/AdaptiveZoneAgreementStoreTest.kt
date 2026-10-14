package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveZoneAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveZoneAgreementStore()
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
