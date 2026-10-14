package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalViolationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalViolationQuotaStore()
        val item = newSeasonalViolationQuota("code-112", "label-363")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
