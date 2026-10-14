package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCoachingLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCoachingLedgerStore()
        val item = newPrimaryCoachingLedger("code-47", "title-161")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
