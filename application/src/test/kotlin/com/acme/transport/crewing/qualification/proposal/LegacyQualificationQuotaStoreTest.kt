package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyQualificationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyQualificationQuotaStore()
        val item = newLegacyQualificationQuota("code-897", "title-122")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
