package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDefectAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDefectAgreementStore()
        val item = newManualDefectAgreement("code-749", "title-743")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
