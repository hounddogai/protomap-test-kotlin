package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalViolationProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalViolationProfileStore()
        val item = newRegionalViolationProfile("code-389", "name-67")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
