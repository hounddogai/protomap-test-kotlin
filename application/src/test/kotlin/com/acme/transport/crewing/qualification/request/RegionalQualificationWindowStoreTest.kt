package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalQualificationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalQualificationWindowStore()
        val item = newRegionalQualificationWindow("code-303", "name-703")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
