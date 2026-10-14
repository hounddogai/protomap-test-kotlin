package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCapacityAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCapacityAgreementStore()
        val item = newSeasonalCapacityAgreement("code-385", "name-926")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
