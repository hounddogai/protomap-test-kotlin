package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationLedgerStore()
        val item = newQualificationLedger("code-784", "name-388")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
