package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionLedgerStore()
        val item = newInspectionLedger("code-163", "name-611")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
