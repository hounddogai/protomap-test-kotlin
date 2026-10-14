package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffAssignmentStore()
        val item = newTariffAssignment("code-994", "name-510")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
