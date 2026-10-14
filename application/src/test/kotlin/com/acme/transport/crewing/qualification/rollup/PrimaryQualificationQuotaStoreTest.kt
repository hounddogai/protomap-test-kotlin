package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryQualificationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryQualificationQuotaStore()
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
