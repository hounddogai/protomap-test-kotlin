package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictQualificationLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictQualificationLedgerStore()
        val item = newStrictQualificationLedger("code-464", "name-34")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
