package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTireAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTireAgreementStore()
        val item = newPendingTireAgreement("code-225", "title-127")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
