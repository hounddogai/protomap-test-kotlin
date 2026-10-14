package com.acme.transport.crewing.qualification.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalQualificationExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalQualificationExceptionStore()
        val item = newRegionalQualificationException("code-159", "label-854")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
